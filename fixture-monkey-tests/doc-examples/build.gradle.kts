subprojects {
	afterEvaluate {
		listOf("classes", "testClasses", "processResources", "processTestResources", "jar").forEach { taskName ->
			tasks.findByName(taskName)?.enabled = true
		}
	}

	dependencies {
		val libs = rootProject.libs
		val projects = rootProject.projects

		testImplementation(projects.fixtureMonkey)
		testImplementation(projects.fixtureMonkeyTests)
		testImplementation(libs.junit.jupiter.api)
		testImplementation(libs.junit.jupiter.engine)
		testImplementation(libs.assertj.core)
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}
