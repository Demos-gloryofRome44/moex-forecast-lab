# quotelab — оценка прогнозов по котировкам MOEX

Сервис фиксирует прогнозы торговых стратегий по бумагам MOEX в момент выдачи, сам оценивает их, когда наступает горизонт, и показывает честный рейтинг на фоне простых бейзлайнов. Это исследовательская площадка.

> **Не инвестиционная рекомендация.** Вся торговля бумажная, данные — открытые MOEX ISS (задержка около 15 минут).

Как пользоваться интерфейсом, со скриншотами — [docs/USER_GUIDE.md](docs/USER_GUIDE.md). Как в проекте используется Kora — [docs/KORA.md](docs/KORA.md).

Backend на Kotlin и [Kora](https://github.com/kora-projects/kora) (DI на этапе компиляции через KSP), PostgreSQL, фронт на Angular 22.

## Быстрый старт (Docker)

Нужен только Docker.

```bash
docker compose up -d --build
```

| Что | Адрес |
|---|---|
| Фронт | http://localhost:4200 |
| REST API | http://localhost:8080/api |
| Метрики Prometheus | http://localhost:8085/metrics |
| PostgreSQL | localhost:5432 (quotelab / quotelab) |

При старте backend применяет миграции, регистрирует версии стратегий, сидирует watchlist (SBER, GAZP, LKOH, ROSN, IMOEX), подгружает свечи за 2 года с MOEX и выдаёт живые прогнозы на последний торговый день. Логи: `docker compose logs -f backend`.

Живой журнал наполняется оценками по мере наступления горизонтов (1, 5, 20 торговых дней). Чтобы сразу увидеть рейтинг, запустите историческую прогонку (отдельный режим, всегда с пометкой «бэктест»):

```bash
curl -X POST "localhost:8080/api/admin/backtest?from=2025-10-20"
```

Год по пяти бумагам считается примерно за минуту; результат — в рейтинге в режиме «Бэктест».

Остановить: `docker compose down`. Остановить и удалить данные: `docker compose down -v`.

Как устроено: сборка идёт внутри Docker, на хосте не нужны ни JDK, ни Node. `backend/Dockerfile` собирает дистрибутив Kora (`installDist`) на JDK 21 и запускает его на JRE 21, кеш Gradle сохраняется между сборками. `frontend/Dockerfile` собирает Angular на Node 24 и отдаёт статику через nginx. nginx проксирует `/api` (включая SSE без буферизации) на backend — см. `frontend/nginx.conf`. Админ-эндпоинты в compose включены (`QUOTELAB_DEV=true`).

## Запуск для разработки

Нужны JDK 21+ (Gradle сам скачает toolchain 21), Docker и Node (любая версия, см. [frontend/README.md](frontend/README.md)).

```bash
docker compose up -d postgres
cd backend && ./gradlew :app:installDist && QUOTELAB_DEV=true app/build/install/quotelab/bin/quotelab
```

```bash
cd frontend && npm install && npm start                # http://localhost:4200, /api → localhost:8080
```

## Проверка

```bash
cd backend && ./gradlew build      # компиляция, detekt, юнит-тесты и интеграционные тесты (@KoraAppTest + Testcontainers)
```

Интеграционные тесты поднимают PostgreSQL в Docker; без Docker они пропускаются.

## Архитектура

```
moex-forecast-lab/
├── backend/                 # Gradle, слои по образцу архива backend
│   ├── buildSrc/            # quotelab.kotlin-conventions, quotelab.kora-conventions, Versions, Dependencies, Modules
│   ├── domain/              # сущности, value class, порты (access), problems, индикаторы и стратегии — без Kora
│   ├── use-case/            # сервисы без аннотаций, DTO Request/Response, движок прогнозов, оценщик, метрики
│   ├── infrastructure/      # Kora JDBC-репозитории + адаптеры портов, клиент MOEX ISS, метрики
│   ├── flyway/              # только SQL-миграции схемы (db/migration), подключается к app как runtimeOnly
│   ├── rest-api/            # контроллеры, тела @Json с валидацией, единый обработчик ошибок, SSE, кеш метрик
│   └── app/                 # @KoraApp, UseCaseModule (аналог UseCaseConfig), джобы, сидер, application.yaml
├── frontend/                # Angular 22
├── docs/                   # USER_GUIDE.md — руководство пользователя, KORA.md — как используется Kora
└── docker-compose.yml       # PostgreSQL + backend + frontend (nginx)
```

Зависимости направлены внутрь, к `domain`. Всё, что связано с Kora, находится в `infrastructure`, `rest-api` и `app`.

### Жизненный цикл прогноза

1. **CandleSyncJob** (будни 19:15 МСК) дозагружает свечи. Повторная загрузка идемпотентна: upsert по `(secid, interval, begin_at)`.
2. Сразу после успешной синхронизации **ForecastRunJob** закрывает всё, что уже можно оценить, обновляет веса ансамбля и выдаёт прогнозы. Каждая стратегия получает `CandleSeries.asOf(T)`, у которой нет доступа к свечам после T.
3. **EvaluationJob** (ежечасно) находит N-ю свечу после дня выдачи и пишет оценку (HIT, MISS или NEUTRAL, доходность, Brier), затем **EnsembleWeightsJob** обновляет веса.
4. Метрики и рейтинг пересчитываются из оценок; кеш Caffeine сбрасывается при новой оценке.

Прогноз неизменяем: триггер в БД запрещает менять любые поля, кроме статуса (только `PENDING → EVALUATED`), а также `DELETE` и `TRUNCATE`. Оценки и веса ансамбля только добавляются.

### Стратегии

| Код | Тип | Идея |
|---|---|---|
| `baseline-always-up` | бейзлайн | всегда UP |
| `baseline-last-direction` | бейзлайн | «завтра как вчера» |
| `baseline-random` | бейзлайн | монетка с фиксированным seed (зависит только от seed, бумаги, даты и горизонта) |
| `sma-crossover` | индикатор | SMA(10) против SMA(30) |
| `rsi` | индикатор | возврат к среднему: RSI(14) < 30 → UP, > 70 → DOWN, иначе воздержание |
| `macd` | индикатор | знак гистограммы MACD(12, 26, 9) |
| `bollinger` | индикатор | выход за полосы (20, 2σ) → ставка на возврат |
| `ensemble-mw` | ансамбль | смесь вероятностей индикаторных стратегий, веса — multiplicative weights (Hedge) |

Параметры задаются в `application.yaml`. Если поменять параметры без смены версии, сервис не стартует: старая версия и её прогнозы остаются в БД неизменными.

### Методология

- **Нет look-ahead.** Это обеспечено типом `CandleSeries` и проверено тестом, который подаёт «будущие» свечи.
- **Горизонт считается в торговых днях по свечам.** Сессии выходного дня MOEX по умолчанию не считаются торговыми днями (`includeWeekendSessions`).
- **Оценка.** `r = (P_target − P_base) / P_base`; при |r| < 0,1% исход NEUTRAL. FLAT — это воздержание: такой исход тоже NEUTRAL. Brier `(p − y)²` считается только для HIT и MISS.
- **Рейтинг.** Hit rate показывается с 95%-интервалом Уилсона и всегда вместе с размером выборки. Пометка «лучше монетки» ставится, только если нижняя граница интервала выше 50% при n ≥ 30. При n < 30 стоит пометка «мало данных». Над таблицей предупреждение о множественных сравнениях.
- **Бумажная торговля.** Каждый прогноз — транш 1/h капитала, комиссия и проскальзывание по 0,05% за сторону, Sharpe и максимальная просадка.
- **Ансамбль.** Потеря: HIT — 0, MISS — 1, воздержание — 0,5. Без штрафа за воздержание почти всегда FLAT-стратегии забрали бы весь вес. Веса пересчитываются walk-forward: только по оценкам, сделанным не позже момента выдачи. История весов хранится.
- **Бэктест.** Отдельный `origin = BACKTEST`, время выдачи и оценки симулируется концом торгового дня. С живым журналом не смешивается.

## REST API

Префикс `/api`, ошибки — `{"error": "...", "details": [...]}`. Прогнозы доступны только на чтение.

| Метод и путь | Назначение |
|---|---|
| `GET /api/instruments` | watchlist |
| `POST /api/instruments` `{"secid": "YDEX"}` | добавить бумагу (проверка на MOEX) |
| `GET /api/instruments/{secid}/candles?from&till&interval` | свечи |
| `GET /api/strategies`, `GET /api/strategies/{id}` | стратегии и версии |
| `GET /api/strategies/{id}/performance?secid&horizon&from&till&origin` | hit rate с интервалом, Brier, Sharpe, просадка, n |
| `GET /api/strategies/{id}/calibration?horizon&origin` | калибровка по корзинам уверенности |
| `GET /api/strategies/{id}/equity?secid&horizon&origin` | кривая капитала |
| `GET /api/leaderboard?horizon&from&till&origin` | рейтинг с бейзлайнами |
| `GET /api/forecasts?secid&strategyId&status&horizon&origin&from&till&page&size` | журнал (size ≤ 200) |
| `GET /api/forecasts/{id}` | один прогноз с оценкой |
| `GET /api/stream` | SSE: `forecast.created`, `forecast.evaluated`, `candle.synced` |
| `POST /api/admin/sync`, `/run-forecasts`, `/evaluate`, `/backtest?from&till` | ручной запуск, только при `QUOTELAB_DEV=true` |

`origin` принимает `LIVE` (по умолчанию) или `BACKTEST`.

Служебный порт 8085: `/metrics` (Prometheus, включая `quotelab_*`), `/system/readiness`, `/system/liveness`.

## Конфигурация

`backend/app/src/main/resources/application.yaml`. Значения вида `${VAR:default}` берутся из переменной окружения `VAR`, иначе используется `default`. Основные переменные окружения: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `HTTP_PORT`, `MOEX_ISS_URL`, `QUOTELAB_DEV`, `QUOTELAB_JOBS_ENABLED`, `QUOTELAB_SYNC_ON_STARTUP`.