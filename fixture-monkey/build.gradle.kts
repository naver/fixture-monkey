plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    runtimeOnly(project(":fixture-monkey-engine"))
    api(project(":fixture-monkey-api"))

    api("net.jqwik:jqwik:${Versions.JQWIK}")
    api("com.github.curious-odd-man:rgxgen:1.4")
    api("com.github.mifmif:generex:1.0.2")

    testRuntimeOnly(project(":fixture-monkey-engine"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}
