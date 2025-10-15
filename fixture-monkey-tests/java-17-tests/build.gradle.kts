plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

dependencies {
    testImplementation(projects.fixtureMonkeyJackson)
}
