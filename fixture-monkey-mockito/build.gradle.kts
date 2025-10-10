plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(projects.fixtureMonkey)
    api(libs.mockito.core)

    testImplementation(libs.assertj.core)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}



