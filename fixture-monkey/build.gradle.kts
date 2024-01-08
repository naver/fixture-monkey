plugins {
    id("me.champeau.jmh") version "0.7.2"
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    runtimeOnly(project(":fixture-monkey-engine"))
    api(project(":fixture-monkey-api"))

    api("net.jqwik:jqwik:${Versions.JQWIK}")
    api("com.github.mifmif:generex:1.0.2")

    testRuntimeOnly(project(":fixture-monkey-engine"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")

    jmhImplementation(project(":fixture-monkey-javax-validation"))
    jmhImplementation(project(":fixture-monkey-jackson"))
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
