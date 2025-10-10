plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkey)

    implementation(libs.kotlin.reflect)

    testImplementation(projects.fixtureMonkeyJackson)
    testImplementation(projects.fixtureMonkeyJavaxValidation)
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
}
