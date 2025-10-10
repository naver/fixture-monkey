plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkeyApi)
    api(libs.hibernate.validator7)
    api(libs.jakarta.validation.api)
    api(libs.jakarta.el4)

    testImplementation(projects.fixtureMonkey)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}


