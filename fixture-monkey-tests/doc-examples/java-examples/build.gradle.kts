plugins {
	java
}

dependencies {
	testImplementation(projects.fixtureMonkeyJavaxValidation)
	testImplementation(projects.fixtureMonkeyJakartaValidation)
	testImplementation(projects.fixtureMonkeyJackson)
	testImplementation(projects.fixtureMonkeyDatafaker)
	testImplementation(libs.lombok)
	testAnnotationProcessor(libs.lombok)
}

tasks.withType<Test> {
	useJUnitPlatform {
		includeEngines("junit-jupiter")
	}
}
