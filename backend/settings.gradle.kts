plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "quotelab"

include(":domain")
include(":use-case")
include(":infrastructure")
include(":flyway")
include(":rest-api")
include(":app")
