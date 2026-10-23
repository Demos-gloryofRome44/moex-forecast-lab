import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

group = "ru.quotelab"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JAVA))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.fromTarget(Versions.JAVA.toString()))
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("detekt.yml"))
    source.setFrom("src/main/kotlin", "src/test/kotlin")
}

dependencies {
    testImplementation(platform(Dependencies.Test.JUNIT_BOM))
    testImplementation(Dependencies.Test.JUNIT_JUPITER)
    testImplementation(Dependencies.Test.KOTLIN_TEST)
    testRuntimeOnly(Dependencies.Test.JUNIT_LAUNCHER)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
