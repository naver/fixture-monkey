plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.reflect)

    testImplementation(projects.fixtureMonkeyKotlin)
    testImplementation(projects.fixtureMonkeyKotest)
    testImplementation(projects.fixtureMonkeyJavaxValidation)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property.arbs)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
