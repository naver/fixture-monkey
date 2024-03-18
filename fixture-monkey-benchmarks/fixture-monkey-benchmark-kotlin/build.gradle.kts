plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey-kotlin"))
    jmhImplementation(testFixtures(project(":fixture-monkey-benchmarks:fixture-monkey-benchmark")))
}

jmh {
    fork = 1
    warmupIterations = 3
    iterations = 10
    resultFormat = "CSV"
}
