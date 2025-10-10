plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkeyKotlin)
    api(libs.kotest.property.jvm)

    implementation(libs.kotlin.reflect)
}
