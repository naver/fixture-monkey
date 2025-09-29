plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    runtimeOnly(project(":fixture-monkey-engine"))
    api(project(":fixture-monkey-api"))
    api(libs.slf4j.api)
    api(libs.jqwik)
    api(libs.rgxgen)
    api(libs.generex)

    testRuntimeOnly(project(":fixture-monkey-engine"))
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
