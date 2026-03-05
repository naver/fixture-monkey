plugins {
    alias(libs.plugins.mrjar)
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.object-farm-maven-publish-conventions")
}

version = findProperty("objectFarmVersion") as String

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

            val checkerFrameworkExtension = project.extensions.findByType<org.checkerframework.gradle.plugin.CheckerFrameworkExtension>()
            checkerFrameworkExtension?.skipCheckerFramework = true
        }
}

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.1")
    testImplementation(libs.lombok)
    testImplementation(libs.assertj.core)
    testAnnotationProcessor(libs.lombok)
}

configurations.compileOnly {
    exclude(group = "org.junit.platform", module = "junit-platform-commons")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}
