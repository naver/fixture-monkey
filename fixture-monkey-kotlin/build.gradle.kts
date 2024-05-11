plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}")

    testImplementation(project(":fixture-monkey-jackson"))
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
}
