-- Все моменты времени — timestamptz (UTC), торговые даты — отдельными date-колонками.

CREATE TABLE instruments
(
    secid     text PRIMARY KEY,
    board     text    NOT NULL,
    name      text    NOT NULL,
    is_active boolean NOT NULL DEFAULT true
);

CREATE TABLE candles
(
    secid      text          NOT NULL REFERENCES instruments (secid),
    interval   int           NOT NULL,
    begin_at   timestamptz   NOT NULL,
    trade_date date          NOT NULL,
    open       numeric(18, 6) NOT NULL CHECK (open > 0),
    high       numeric(18, 6) NOT NULL CHECK (high > 0),
    low        numeric(18, 6) NOT NULL CHECK (low > 0),
    close      numeric(18, 6) NOT NULL CHECK (close > 0),
    volume     bigint        NOT NULL CHECK (volume >= 0),
    PRIMARY KEY (secid, interval, begin_at)
);

CREATE INDEX candles_trade_date_idx ON candles (secid, interval, trade_date);

CREATE TABLE strategies
(
    id         uuid PRIMARY KEY,
    code       text        NOT NULL,
    version    text        NOT NULL,
    kind       text        NOT NULL CHECK (kind IN ('INDICATOR', 'BASELINE', 'ENSEMBLE', 'ML')),
    params     jsonb       NOT NULL DEFAULT '{}'::jsonb,
    is_active  boolean     NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    UNIQUE (code, version)
);

CREATE TABLE forecasts
(
    id                   uuid PRIMARY KEY,
    strategy_id          uuid           NOT NULL REFERENCES strategies (id),
    secid                text           NOT NULL REFERENCES instruments (secid),
    horizon_days         int            NOT NULL CHECK (horizon_days > 0),
    issued_at            timestamptz    NOT NULL,
    trade_date           date           NOT NULL,
    base_price           numeric(18, 6) NOT NULL CHECK (base_price > 0),
    direction            text           NOT NULL CHECK (direction IN ('UP', 'DOWN', 'FLAT')),
    confidence           double precision NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    -- Оценка цели по будням; фактическая дата цели (по свечам) лежит в evaluations.target_trade_date.
    expected_target_date date           NOT NULL,
    status               text           NOT NULL CHECK (status IN ('PENDING', 'EVALUATED')),
    origin               text           NOT NULL CHECK (origin IN ('LIVE', 'BACKTEST')),
    UNIQUE (strategy_id, secid, horizon_days, trade_date, origin)
);

CREATE INDEX forecasts_pending_idx ON forecasts (origin, id) WHERE status = 'PENDING';
CREATE INDEX forecasts_journal_idx ON forecasts (trade_date DESC, issued_at DESC);
CREATE INDEX forecasts_strategy_idx ON forecasts (strategy_id, origin, horizon_days);

-- Главный инвариант: прогноз неизменяем. Меняется только статус и только PENDING -> EVALUATED.
CREATE FUNCTION forecasts_immutable() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'forecasts are append-only: DELETE is forbidden (forecast %)', OLD.id;
    END IF;
    IF (NEW.id, NEW.strategy_id, NEW.secid, NEW.horizon_days, NEW.issued_at, NEW.trade_date, NEW.base_price,
        NEW.direction, NEW.confidence, NEW.expected_target_date, NEW.origin)
        IS DISTINCT FROM
       (OLD.id, OLD.strategy_id, OLD.secid, OLD.horizon_days, OLD.issued_at, OLD.trade_date, OLD.base_price,
        OLD.direction, OLD.confidence, OLD.expected_target_date, OLD.origin) THEN
        RAISE EXCEPTION 'forecasts are immutable: only status may change (forecast %)', OLD.id;
    END IF;
    IF NEW.status IS DISTINCT FROM OLD.status AND NOT (OLD.status = 'PENDING' AND NEW.status = 'EVALUATED') THEN
        RAISE EXCEPTION 'forecast status may only change PENDING -> EVALUATED (forecast %: % -> %)', OLD.id, OLD.status, NEW.status;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER forecasts_immutable
    BEFORE UPDATE OR DELETE
    ON forecasts
    FOR EACH ROW
EXECUTE FUNCTION forecasts_immutable();

CREATE FUNCTION forbid_truncate() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION '% is append-only: TRUNCATE is forbidden', TG_TABLE_NAME;
END;
$$;

CREATE TRIGGER forecasts_no_truncate
    BEFORE TRUNCATE
    ON forecasts
    FOR EACH STATEMENT
EXECUTE FUNCTION forbid_truncate();

CREATE TABLE evaluations
(
    forecast_id       uuid PRIMARY KEY REFERENCES forecasts (id),
    evaluated_at      timestamptz    NOT NULL,
    target_trade_date date           NOT NULL,
    actual_price      numeric(18, 6) NOT NULL CHECK (actual_price > 0),
    actual_return     numeric(20, 10) NOT NULL,
    outcome           text           NOT NULL CHECK (outcome IN ('HIT', 'MISS', 'NEUTRAL')),
    brier             numeric(20, 10) CHECK (brier IS NULL OR (brier >= 0 AND brier <= 1))
);

CREATE INDEX evaluations_evaluated_at_idx ON evaluations (evaluated_at);

-- Оценка тоже не переписывается.
CREATE FUNCTION append_only() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION '% is append-only: % is forbidden', TG_TABLE_NAME, TG_OP;
END;
$$;

CREATE TRIGGER evaluations_append_only
    BEFORE UPDATE OR DELETE
    ON evaluations
    FOR EACH ROW
EXECUTE FUNCTION append_only();

CREATE TABLE ensemble_weights
(
    id                 bigserial PRIMARY KEY,
    ensemble_id        uuid        NOT NULL REFERENCES strategies (id),
    member_strategy_id uuid        NOT NULL REFERENCES strategies (id),
    horizon_days       int         NOT NULL,
    origin             text        NOT NULL CHECK (origin IN ('LIVE', 'BACKTEST')),
    effective_at       timestamptz NOT NULL,
    weight             numeric     NOT NULL CHECK (weight >= 0),
    created_at         timestamptz NOT NULL DEFAULT now(),
    UNIQUE (ensemble_id, member_strategy_id, horizon_days, origin, effective_at)
);

CREATE INDEX ensemble_weights_lookup_idx ON ensemble_weights (ensemble_id, horizon_days, origin, effective_at);

CREATE TRIGGER ensemble_weights_append_only
    BEFORE UPDATE OR DELETE
    ON ensemble_weights
    FOR EACH ROW
EXECUTE FUNCTION append_only();
