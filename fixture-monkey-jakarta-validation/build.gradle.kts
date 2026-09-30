plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkeyApi)
    testImplementation(libs.hibernate.validator7)
    compileOnly(libs.jakarta.validation.api)
    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.jakarta.el4)

    testImplementation(projects.fixtureMonkey)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}


