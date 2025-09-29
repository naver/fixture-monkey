allprojects {
    tasks.filter { it.name != "test" && !it.name.startsWith("compile") }
        .forEach { it.enabled = false }
}

subprojects {
    dependencies {
        val libs = rootProject.libs

        testImplementation(project(":fixture-monkey"))
        testImplementation(project(":fixture-monkey-tests"))
        testImplementation(libs.junit.jupiter.api)
        testImplementation(libs.junit.jupiter.engine)
        testImplementation(libs.assertj.core)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
