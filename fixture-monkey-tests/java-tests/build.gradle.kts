plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testImplementation(project(":fixture-monkey-jackson"))
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}
