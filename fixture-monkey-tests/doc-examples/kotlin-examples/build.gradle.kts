plugins {
	alias(libs.plugins.kotlin.jvm)
}

dependencies {
	implementation(libs.kotlin.reflect)

	testImplementation(projects.fixtureMonkeyKotlin)
	testImplementation(projects.fixtureMonkeyKotest)
	testImplementation(projects.fixtureMonkeyJakartaValidation)
	testImplementation(projects.fixtureMonkeyJackson)
	testImplementation(projects.fixtureMonkeyDatafaker)
	testImplementation(libs.kotest.runner.junit5)
	testImplementation(libs.kotest.assertions.core)
	testImplementation(libs.lombok)
	testAnnotationProcessor(libs.lombok)
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}
