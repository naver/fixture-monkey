plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

val checkerFrameworkExtension =
    project.extensions.findByType<org.checkerframework.gradle.plugin.CheckerFrameworkExtension>()
checkerFrameworkExtension?.skipCheckerFramework = true

dependencies {
    api(projects.fixtureMonkey)
}

jmh {
    fork = 1
    warmupIterations = 3
    iterations = 10
    resultFormat = "CSV"
}
