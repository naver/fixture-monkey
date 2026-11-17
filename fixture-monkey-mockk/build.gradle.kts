plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkey)
    api(libs.mockk)

    implementation(libs.kotlin.reflect)

    testImplementation(projects.fixtureMonkeyKotlin)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
}
