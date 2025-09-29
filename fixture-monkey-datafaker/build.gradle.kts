plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey-api"))
    implementation(libs.datafaker)
    implementation(libs.kotlin.reflect)

    testAnnotationProcessor(libs.lombok)

    testImplementation(project(":fixture-monkey"))
    testImplementation(libs.lombok)
    implementation(libs.kotlin.stdlib.jdk8)
}
