plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

dependencies {
    api(projects.fixtureMonkeyKotlin)
    jmhImplementation(testFixtures(projects.fixtureMonkeyBenchmarks.fixtureMonkeyBenchmark))
}

jmh {
    fork = 1
    warmupIterations = 3
    iterations = 10
    resultFormat = "CSV"
}
