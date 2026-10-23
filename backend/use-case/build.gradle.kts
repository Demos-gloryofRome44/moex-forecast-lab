plugins {
    id("quotelab.kotlin-conventions")
}

dependencies {
    implementation(project(Modules.DOMAIN))

    implementation(Dependencies.Logging.SLF4J_API)
}
