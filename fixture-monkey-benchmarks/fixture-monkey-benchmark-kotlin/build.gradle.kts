plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
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
