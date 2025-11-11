plugins {
    alias(libs.plugins.mrjar)
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

            val checkerFrameworkExtension = project.extensions.findByType<org.checkerframework.gradle.plugin.CheckerFrameworkExtension>()
            checkerFrameworkExtension?.skipCheckerFramework = true
        }
}

dependencies {
    api(libs.apiguardian.api)
    api(projects.objectFarmApi)

    compileOnly(libs.jqwik.engine)
    compileOnly(libs.jqwik.api)
    compileOnly(libs.jqwik.web)
    compileOnly(libs.jqwik.time)
    compileOnly(libs.rgxgen)
    compileOnly(libs.generex)

    testImplementation(libs.jqwik.engine)
    testImplementation(libs.jqwik.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.engine)
    testImplementation(libs.lombok)
    testImplementation(libs.assertj.core)
    testImplementation(libs.jqwik.time)
    testImplementation(libs.rgxgen)
    testAnnotationProcessor(libs.lombok)
}

configurations.compileOnly {
    exclude(group = "org.junit.platform", module = "junit-platform-commons")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

