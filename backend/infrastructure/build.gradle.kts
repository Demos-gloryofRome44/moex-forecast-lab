plugins {
    id("quotelab.kora-conventions")
}

dependencies {
    implementation(project(Modules.DOMAIN))
    implementation(project(Modules.USE_CASE))

    implementation(Dependencies.Kora.DATABASE_JDBC)
    implementation(Dependencies.Kora.HTTP_CLIENT)
    implementation(Dependencies.Kora.JSON)
    implementation(Dependencies.Kora.RESILIENT)
    implementation(Dependencies.Kora.MICROMETER)
    implementation(Dependencies.Kora.CONFIG_COMMON)
    implementation(Dependencies.Logging.SLF4J_API)
    runtimeOnly(Dependencies.Database.POSTGRESQL)
}
