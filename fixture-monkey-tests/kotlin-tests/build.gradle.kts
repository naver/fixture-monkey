plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.reflect)

    testImplementation(project(":fixture-monkey-kotlin"))
    testImplementation(project(":fixture-monkey-kotest"))
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property.arbs)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
