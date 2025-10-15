plugins {
    java
    `java-library`
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "com.navercorp.fixturemonkey"
    version = "1.1.16-SNAPSHOT"
}

subprojects {
    apply {
        plugin("java")
        plugin("java-library")
        plugin("com.navercorp.fixturemonkey.gradle.plugin.optional-dependencies")
    }

    dependencies {
        val libs = rootProject.libs

        api(libs.google.jsr305)
        implementation(libs.google.findbugs.annotations)
        implementation(libs.jspecify)
        compileOnly(libs.slf4j.api)
        testImplementation(libs.logback.classic)
    }

    tasks.javadoc { enabled = false }

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(8) }
        withJavadocJar()
        withSourcesJar()
    }

    tasks.jar {
        val artifactName: String? by project
        if (artifactName == null) {
            return@jar
        }

        manifest {
            attributes(
                "Specification-Title" to artifactName,
                "Specification-Version" to project.version,
                "Specification-Vendor" to "com.navercorp",
                "Implementation-Title" to artifactName,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "com.navercorp",
            )
        }
    }

    if (!name.endsWith("tests")) {
        tasks.withType<Test> {
            useJUnitPlatform {
                includeEngines("jqwik")
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype { // only for users registered in Sonatype after 24 Feb 2021
            val ossrhUsernameToken: String = System.getenv("OSSRH_USERNAME_TOKEN") ?: ""
            val ossrhPasswordToken: String = System.getenv("OSSRH_PASSWORD_TOKEN") ?: ""

            nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
            snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
            username = ossrhUsernameToken
            password = ossrhPasswordToken
        }
    }
}
