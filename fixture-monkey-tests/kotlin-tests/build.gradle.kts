plugins {
    alias(libs.plugins.kotlin.jvm)
}


dependencies {
    implementation(libs.kotlin.reflect)

    testImplementation(projects.fixtureMonkeyKotlin)
    testImplementation(projects.fixtureMonkeyKotest)
    testImplementation(projects.fixtureMonkeyJavaxValidation)
    testImplementation(projects.fixtureMonkeyJakartaValidation)
    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.hibernate.validator7)
    testImplementation(libs.jakarta.el4)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property.arbs)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
