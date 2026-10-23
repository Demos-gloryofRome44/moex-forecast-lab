plugins {
    id("quotelab.kora-conventions")
}

dependencies {
    implementation(project(Modules.DOMAIN))
    implementation(project(Modules.USE_CASE))

    implementation(Dependencies.Kora.HTTP_SERVER)
    implementation(Dependencies.Kora.JSON)
    implementation(Dependencies.Kora.VALIDATION)
    implementation(Dependencies.Kora.CACHE_CAFFEINE)
    implementation(Dependencies.Kora.CONFIG_COMMON)
    implementation(Dependencies.Logging.SLF4J_API)
}
