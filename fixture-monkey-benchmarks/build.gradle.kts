plugins {
    alias(libs.plugins.jmh)
    `java-test-fixtures`
}

tasks.jmh {
    enabled = false
}

allprojects {
    tasks.filter { it.name != "jmh" && !it.name.startsWith("compile") && !it.name.startsWith("jar") }
        .forEach { it.enabled = false }
}

subprojects {
    plugins.apply("me.champeau.jmh")
    plugins.apply("java-test-fixtures")

    dependencies {
        val libs = rootProject.libs

        jmhImplementation(project(":fixture-monkey-javax-validation"))
        jmhImplementation(project(":fixture-monkey-jackson"))
        jmhImplementation(libs.jmh.core)
        jmhImplementation(libs.jmh.generator.annprocess)
        jmhImplementation(libs.lombok)
        jmhAnnotationProcessor(libs.lombok)

        testFixturesApi(project(":fixture-monkey-javax-validation"))
        testFixturesApi(libs.jmh.core)
        testFixturesApi(libs.jmh.generator.annprocess)
        testFixturesApi(libs.lombok)
        testFixturesAnnotationProcessor(libs.lombok)
    }
}
