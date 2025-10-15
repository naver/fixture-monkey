plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkey)
    api(libs.junit.jupiter.api)

    testImplementation(projects.fixtureMonkeyJavaxValidation)
    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testImplementation(libs.kotlin.stdlib)
    testAnnotationProcessor(libs.lombok)
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}
