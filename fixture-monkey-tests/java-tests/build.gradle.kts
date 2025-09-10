plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation(project(":fixture-monkey-datafaker"))
    testImplementation(project(":fixture-monkey-jackson"))
    testImplementation("org.jspecify:jspecify:${Versions.JSPECIFY}")
    testImplementation("org.checkerframework:checker-qual:${Versions.CHECKER_QUAL}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}
