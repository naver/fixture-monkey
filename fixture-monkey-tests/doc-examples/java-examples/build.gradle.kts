plugins {
	java
}

dependencies {
	testImplementation(projects.fixtureMonkeyJavaxValidation)
	testImplementation(projects.fixtureMonkeyJakartaValidation)
	testImplementation(libs.jakarta.validation.api)
	testImplementation(libs.hibernate.validator7)
	testImplementation(libs.jakarta.el4)
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
