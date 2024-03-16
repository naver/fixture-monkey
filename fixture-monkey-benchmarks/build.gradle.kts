plugins {
    id("me.champeau.jmh") version "0.7.2"
    `java-test-fixtures`
}

tasks.jmh{
    enabled = false
}

subprojects {
    tasks.filter { it.name != "jmh" && !it.name.startsWith("compile") && it.name != "jar" }
        .forEach { it.enabled = false }

    plugins.apply("me.champeau.jmh")
    plugins.apply("java-test-fixtures")

    dependencies {
        jmhImplementation(project(":fixture-monkey-javax-validation"))
        jmhImplementation(project(":fixture-monkey-jackson"))
        jmhImplementation("org.openjdk.jmh:jmh-core:${Versions.JMH}")
        jmhImplementation("org.openjdk.jmh:jmh-generator-annprocess:${Versions.JMH}")
        jmhImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
        jmhAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")

        testFixturesApi(project(":fixture-monkey-javax-validation"))
        testFixturesApi("org.openjdk.jmh:jmh-core:${Versions.JMH}")
        testFixturesApi("org.openjdk.jmh:jmh-generator-annprocess:${Versions.JMH}")
        testFixturesApi("org.projectlombok:lombok:${Versions.LOMBOK}")
        testFixturesAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
    }
}
