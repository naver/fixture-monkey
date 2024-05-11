plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT
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
