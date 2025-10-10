plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkey)
    api(libs.autoparams)
    compileOnly(projects.fixtureMonkeyEngine)

    testImplementation(projects.fixtureMonkeyEngine)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
