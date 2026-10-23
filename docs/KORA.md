# Kora в quotelab

Backend quotelab написан на [Kora](https://github.com/kora-projects/kora) — Kotlin/Java-фреймворке Т-Банка, который собирает приложение на этапе компиляции. Версия — Kora 1.2.23 (`ru.tinkoff.kora`), Kotlin 1.9.25, KSP 1.9.25-1.0.20, Java 21.

## Зачем Kora

Освоить Kora было одной из целей проекта. Для этого сервис специально задействует типовые возможности бэкенда: БД, HTTP-сервер и HTTP-клиент, планировщик, отказоустойчивость, кеш, конфигурацию, телеметрию и тесты.

Что даёт Kora:
- **Граф зависимостей собирается при компиляции.** KSP генерирует код графа, контроллеров, JDBC-репозиториев, HTTP-клиента и JSON-кодеков. Ошибка «не хватает зависимости» — это ошибка сборки, а не падение при старте.
- **Нет рефлексии и сканирования classpath во время работы.** Приложение стартует меньше чем за секунду.
- **Код, который можно прочитать.** Всё сгенерированное лежит в `build/generated/ksp` и читается как обычный Kotlin.

## Как устроен граф

```
QuotelabApplication (@KoraApp, app)
├── модули Kora: YAML-конфиг, JSON, валидация, Undertow, JDK HTTP-клиент, JDBC, Flyway,
│                Quartz, отказоустойчивость, Caffeine, Logback, Micrometer
├── InfrastructureModule (@KoraSubmodule, infrastructure) — все @Component модуля
├── RestApiModule (@KoraSubmodule, rest-api)             — все @Component модуля
└── UseCaseModule (@Module, app)                         — фабрики use-case-сервисов
```

- Модули `domain` и `use-case` от Kora не зависят: в них нет ни одной аннотации фреймворка.
- Use-case-сервисы — обычные классы. Их создают фабричные методы `UseCaseModule`, так же как `UseCaseConfig` создавал бины в архиве backend.
- Компоненты из других Gradle-модулей попадают в граф через `@KoraSubmodule`. KSP генерирует для каждого модуля интерфейс `*SubmoduleImpl`, и `@KoraApp` его наследует.
- Точка входа: `KoraApplication.run { QuotelabApplicationGraph.graph() }`. Класс `QuotelabApplicationGraph` сгенерирован.

## Какие модули Kora используются

| Модуль Kora | Для чего | Где в коде |
|---|---|---|
| `config-yaml` | Типизированная конфигурация из `application.yaml` с переопределением из env (`${VAR:default}`) | `QuotelabConfig`, `MoexConfig`, `ApiConfig` (`@ConfigSource`) |
| `json-module` | JSON без рефлексии: кодеки генерируются для `@Json`-классов | тела `*Body` в `rest-api`, свой `IssResponseJsonReader` для ответа MOEX ISS |
| `validation-module` | Валидация входящих тел (`@Valid`, `@NotBlank`, `@Pattern`, `@Size`) | `AddInstrumentBody`, `InstrumentController` |
| `http-server-undertow` | HTTP-сервер: публичный API на 8080, служебный (метрики, пробы) на 8085 | контроллеры `@HttpController`, `ErrorHandlingInterceptor`, `SseHttpHandlerConfigurer` |
| `http-client-jdk` | Декларативный HTTP-клиент | `MoexIssClient` (`@HttpClient`) |
| `resilient-kora` | `@Retry`, `@Timeout`, `@CircuitBreaker` на методах клиента | `MoexIssClient`, настройки `resilient.*.moex` |
| `database-jdbc` | Репозитории: интерфейс с SQL в `@Query`, реализация генерируется | `*Dao` в `infrastructure`, `JdbcTransactionRunner` |
| `database-flyway` | Применение миграций при старте | SQL в модуле `flyway`, `db/migration` |
| `scheduling-quartz` | Задачи по cron | `CandleSyncJob`, `EvaluationJob` (`@ScheduleWithCron`) |
| `cache-caffeine` | Кеш метрик и рейтинга | `CachedPerformanceQueries` (`@Cacheable`, `@CacheInvalidate`) |
| `micrometer-module` | Метрики Prometheus: встроенные (HTTP, JDBC, клиент) и свои | `MicrometerQuotelabMetrics`, `/metrics` на порту 8085 |
| `logging-logback` | Логи и уровни из конфигурации | `logging.levels` в `application.yaml` |
| `test-junit5` | Интеграционные тесты на реальном графе | `ForecastJournalIntegrationTest` (`@KoraAppTest`) + Testcontainers |

## Сквозные решения

**Ошибки.** Глобальный `HttpServerInterceptor` с тегом `HttpServerModule` превращает доменные исключения в HTTP-статусы с единым телом `ErrorResponse(error, details)`. Это аналог `GlobalExceptionHandler` из архива.

**Отказоустойчивость клиента MOEX.** На методах клиента аспекты объявлены в порядке circuit breaker → retry → timeout, настройки каждого — в `resilient.*.moex`. Если MOEX недоступен, синхронизация отмечает бумагу как неудачную, а сервис продолжает работать.

**Кеш.** Аспекты кеша висят на отдельном классе в `rest-api`, а не на use-case, чтобы use-case оставался без аннотаций. Кеш сбрасывается при каждой новой оценке прогноза.

**SSE.** `/api/stream` обслуживает нативный `ServerSentEventHandler` Undertow. Он подключается через точку расширения Kora `HttpHandlerConfigurer`.

**Конфигурация.** Всё, что меняется между окружениями, читается из `application.yaml` с переопределением `${VAR:default}`: БД, порты, адрес MOEX, cron, параметры стратегий, комиссия, порог флэта.

## Особенности, о которых стоит знать

- Для публичного HTTP-сервера подключается `UndertowHttpServerModule`. `UndertowModule` поднимает только служебный сервер на 8085.
- Аспекты (`@Cacheable`, `@Validate`, `@Retry` на классах) требуют `open`-класса и `open`-методов. KSP напоминает об этом ошибкой сборки.
- У data class для JDBC-результатов стоит `@EntityJdbc`: без неё KSP генерирует мапперы позже, и сборка делает лишний раунд.
- В `scheduling-jdk` нет cron, поэтому используется `scheduling-quartz`. Cron считается в часовом поясе JVM — в `main` он выставлен на `Europe/Moscow`.
- Потоковый ответ `HttpBodyOutput` из блокирующего потока буферизуется без flush, поэтому SSE сделан на нативном хендлере Undertow. Он работает в обход интерсепторов и телеметрии Kora. `shutdownWait = 3s`, чтобы открытые SSE-соединения не задерживали остановку.
- В `@KoraAppTest` поднимаются только компоненты, запрошенные тестом. `@Root`-компоненты (например, стартовый сидер) не выполняются, данные тест готовит сам. Для прямого SQL в тесте инжектится `JdbcDatabase`, а не `JdbcConnectionFactory`.
- Конфиг Kora по умолчанию ищет в `application.yaml` (не `.yml`). В YAML нет `include`, поэтому в тестах значения переопределяются через system properties (`KoraConfigModification.ofSystemProperty`), которые подставляются в `${VAR:default}`.
- В `gradle.properties` стоит `ksp.incremental=false`: инкрементальный KSP иногда терял сгенерированные классы (валидаторы).
