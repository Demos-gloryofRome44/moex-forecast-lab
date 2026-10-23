object Dependencies {

    object Kora {
        const val BOM = "ru.tinkoff.kora:kora-parent:${Versions.KORA}"
        const val SYMBOL_PROCESSORS = "ru.tinkoff.kora:symbol-processors"
        const val COMMON = "ru.tinkoff.kora:common"
        const val CONFIG_COMMON = "ru.tinkoff.kora:config-common"
        const val CONFIG_YAML = "ru.tinkoff.kora:config-yaml"
        const val JSON = "ru.tinkoff.kora:json-module"
        const val VALIDATION = "ru.tinkoff.kora:validation-module"
        const val HTTP_SERVER = "ru.tinkoff.kora:http-server-undertow"
        const val HTTP_CLIENT = "ru.tinkoff.kora:http-client-jdk"
        const val DATABASE_JDBC = "ru.tinkoff.kora:database-jdbc"
        const val DATABASE_FLYWAY = "ru.tinkoff.kora:database-flyway"
        const val SCHEDULING = "ru.tinkoff.kora:scheduling-quartz"
        const val RESILIENT = "ru.tinkoff.kora:resilient-kora"
        const val CACHE_CAFFEINE = "ru.tinkoff.kora:cache-caffeine"
        const val LOGGING_LOGBACK = "ru.tinkoff.kora:logging-logback"
        const val MICROMETER = "ru.tinkoff.kora:micrometer-module"
        const val TEST_JUNIT5 = "ru.tinkoff.kora:test-junit5"
    }

    object Logging {
        const val SLF4J_API = "org.slf4j:slf4j-api:${Versions.SLF4J}"
    }

    object Database {
        const val POSTGRESQL = "org.postgresql:postgresql:${Versions.POSTGRESQL}"
    }

    object Test {
        const val JUNIT_BOM = "org.junit:junit-bom:${Versions.JUNIT}"
        const val JUNIT_JUPITER = "org.junit.jupiter:junit-jupiter"
        const val JUNIT_LAUNCHER = "org.junit.platform:junit-platform-launcher"
        const val KOTLIN_TEST = "org.jetbrains.kotlin:kotlin-test-junit5:${Versions.KOTLIN}"
        const val TESTCONTAINERS_BOM = "org.testcontainers:testcontainers-bom:${Versions.TESTCONTAINERS}"
        const val TESTCONTAINERS_POSTGRESQL = "org.testcontainers:postgresql"
        const val TESTCONTAINERS_JUNIT = "org.testcontainers:junit-jupiter"
    }
}
