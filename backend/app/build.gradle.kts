plugins {
    id("quotelab.kora-application")
}

application {
    applicationName = "quotelab"
    mainClass.set("ru.quotelab.app.QuotelabApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8", "-Duser.timezone=Europe/Moscow")
}

dependencies {
    implementation(project(Modules.DOMAIN))
    implementation(project(Modules.USE_CASE))
    implementation(project(Modules.INFRASTRUCTURE))
    implementation(project(Modules.REST_API))
    runtimeOnly(project(Modules.FLYWAY))

    implementation(Dependencies.Kora.CONFIG_YAML)
    implementation(Dependencies.Kora.JSON)
    implementation(Dependencies.Kora.VALIDATION)
    implementation(Dependencies.Kora.HTTP_SERVER)
    implementation(Dependencies.Kora.HTTP_CLIENT)
    implementation(Dependencies.Kora.DATABASE_JDBC)
    implementation(Dependencies.Kora.DATABASE_FLYWAY)
    implementation(Dependencies.Kora.SCHEDULING)
    implementation(Dependencies.Kora.RESILIENT)
    implementation(Dependencies.Kora.CACHE_CAFFEINE)
    implementation(Dependencies.Kora.LOGGING_LOGBACK)
    implementation(Dependencies.Kora.MICROMETER)
    runtimeOnly(Dependencies.Database.POSTGRESQL)

    testImplementation(Dependencies.Kora.TEST_JUNIT5)
    testImplementation(platform(Dependencies.Test.TESTCONTAINERS_BOM))
    testImplementation(Dependencies.Test.TESTCONTAINERS_POSTGRESQL)
    testImplementation(Dependencies.Test.TESTCONTAINERS_JUNIT)
    testImplementation(Dependencies.Database.POSTGRESQL)
}

tasks.test {
    // Интеграционные тесты поднимают PostgreSQL в Docker; без Docker они пропускаются (disabledWithoutDocker).
    systemProperty("user.timezone", "Europe/Moscow")
}
