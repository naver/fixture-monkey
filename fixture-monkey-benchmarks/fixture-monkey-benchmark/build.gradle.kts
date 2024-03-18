plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    api(project(":fixture-monkey"))
}

jmh {
    fork = 1
    warmupIterations = 3
    iterations = 10
    resultFormat = "CSV"
}
