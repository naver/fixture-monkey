plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation(project(":fixture-monkey-datafaker"))
    testImplementation(project(":fixture-monkey-jackson"))
    testImplementation(libs.jspecify)
    testImplementation(libs.checker.qual)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}
