plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    testImplementation(projects.fixtureMonkeyJavaxValidation)
    testImplementation(projects.fixtureMonkeyDatafaker)
    testImplementation(projects.fixtureMonkeyJackson)
    testImplementation(libs.jspecify)
    testImplementation(libs.checker.qual)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
