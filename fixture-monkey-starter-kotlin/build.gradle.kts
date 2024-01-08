plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey-starter"))
    api(project(":fixture-monkey-kotlin"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
}
