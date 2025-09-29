plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey"))
    api(libs.junit.jupiter.api)

    testImplementation(project(":fixture-monkey-javax-validation"))
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
