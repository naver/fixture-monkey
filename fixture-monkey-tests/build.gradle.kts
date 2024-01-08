allprojects {
    tasks.filter { it.name != "test" && !it.name.startsWith("compile") }
        .forEach { it.enabled = false }
}

subprojects {
    dependencies {
        testImplementation(project(":fixture-monkey"))
        testImplementation(project(":fixture-monkey-tests"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT_JUPITER}")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
        testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
