plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    api(projects.fixtureMonkey)
}

jmh {
    fork = 1
    warmupIterations = 3
    iterations = 10
    resultFormat = "CSV"
}
