plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    runtimeOnly(projects.fixtureMonkeyEngine)
    api(projects.fixtureMonkeyApi)
    api(libs.slf4j.api)
    api(libs.jqwik)
    api(libs.rgxgen)
    api(libs.generex)

    testRuntimeOnly(projects.fixtureMonkeyEngine)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
