plugins {
    id("me.champeau.jmh") version "0.7.2"
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
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

    jmhImplementation(project(":fixture-monkey-jackson"))
    jmhImplementation(project(":fixture-monkey-javax-validation"))
    jmhImplementation(testFixtures(project(":fixture-monkey")))
    jmhImplementation("org.openjdk.jmh:jmh-core:${Versions.JMH}")
    jmhImplementation("org.openjdk.jmh:jmh-generator-annprocess:${Versions.JMH}")
    jmhImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    jmhAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

jmh {
    fork = 1
    warmupIterations = 3
    iterations = 10
    resultFormat = "CSV"
}
