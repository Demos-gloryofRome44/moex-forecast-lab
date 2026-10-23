plugins {
    id("quotelab.kotlin-conventions")
    id("com.google.devtools.ksp")
}

val koraBom: Configuration by configurations.creating

configurations {
    named("implementation") { extendsFrom(koraBom) }
    named("ksp") { extendsFrom(koraBom) }
    named("kspTest") { extendsFrom(koraBom) }
    named("testImplementation") { extendsFrom(koraBom) }
}

dependencies {
    koraBom(platform(Dependencies.Kora.BOM))
    ksp(Dependencies.Kora.SYMBOL_PROCESSORS)
    kspTest(Dependencies.Kora.SYMBOL_PROCESSORS)
}
