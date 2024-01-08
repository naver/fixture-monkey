plugins {
    id("me.champeau.mrjar") version "0.1.1"
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

val multiReleaseVersions = intArrayOf(17)

multiRelease {
    targetVersions(8, *multiReleaseVersions)
}

// Multi release version dependency will extend from default version dependency
multiReleaseVersions.forEach { releaseVersion ->
    configurations.filter { configuration -> configuration.name.startsWith("java$releaseVersion") }
        .forEach { configuration ->
            val defaultVersionConfigurationName = configuration.name
                .removePrefix("java$releaseVersion")
                .replaceFirstChar { it.lowercase() }

            configuration.extendsFrom(configurations.getByName(defaultVersionConfigurationName))
        }
}

dependencies {
    api("org.apiguardian:apiguardian-api:1.1.2")
    compileOnly("net.jqwik:jqwik-engine:${Versions.JQWIK}")
    compileOnly("net.jqwik:jqwik-api:${Versions.JQWIK}")
    compileOnly("net.jqwik:jqwik-web:${Versions.JQWIK}")
    compileOnly("net.jqwik:jqwik-time:${Versions.JQWIK}")
    compileOnly("com.github.mifmif:generex:1.0.2")

    testImplementation("net.jqwik:jqwik-engine:${Versions.JQWIK}")
    testImplementation("net.jqwik:jqwik-api:${Versions.JQWIK}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("net.jqwik:jqwik-time:${Versions.JQWIK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

configurations.compileOnly {
    exclude(group = "org.junit.platform", module = "junit-platform-commons")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

