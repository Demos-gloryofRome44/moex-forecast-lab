plugins {
    `kotlin-dsl`
}

// buildSrc's own script can't see Versions.kt — keep these in sync with it.
val kotlinVersion = "1.9.25"
val kspVersion = "1.9.25-1.0.20"
val detektVersion = "1.23.7"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:$kspVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
}
