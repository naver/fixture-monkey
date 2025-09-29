plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey"))

    implementation(libs.kotlin.reflect)

    testImplementation(project(":fixture-monkey-jackson"))
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
}
