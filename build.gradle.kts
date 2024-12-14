plugins {
    java
    `java-library`
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN apply false
}

allprojects {
    group = "com.navercorp.fixturemonkey"
    version = "1.1.7-SNAPSHOT"
}

subprojects {
    apply {
        plugin("java")
        plugin("java-library")
        plugin("com.navercorp.fixturemonkey.gradle.plugin.optional-dependencies")
    }

    dependencies {
        api("com.google.code.findbugs:jsr305:${Versions.FIND_BUGS_JSR305}")
        implementation("com.google.code.findbugs:findbugs-annotations:${Versions.FIND_BUGS_ANNOTATIONS}")
        compileOnly("org.slf4j:slf4j-api:${Versions.SLF4J}")
        testImplementation("ch.qos.logback:logback-classic:${Versions.LOGBACK}")
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

            nexusUrl = uri("https://oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            username = ossrhUsernameToken
            password = ossrhPasswordToken
        }
    }
}
