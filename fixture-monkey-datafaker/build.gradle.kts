plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkeyApi)
    implementation(libs.datafaker)
    implementation(libs.kotlin.reflect)

    testAnnotationProcessor(libs.lombok)

    testImplementation(projects.fixtureMonkey)
    testImplementation(libs.lombok)
    implementation(libs.kotlin.stdlib.jdk8)
}
