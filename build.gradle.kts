plugins {
    java
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

allprojects {
    group = "com.navercorp.fixturemonkey"
    version = "1.0.8-SNAPSHOT"
}

subprojects {
    apply {
        plugin("java")
        plugin("java-library")
        plugin("maven-publish")
        plugin("signing")
        plugin("com.navercorp.fixturemonkey.gradle.plugin.optional-dependencies")
    }

    dependencies {
        api("com.google.code.findbugs:jsr305:${Versions.FIND_BUGS_JSR305}")
        implementation("com.google.code.findbugs:findbugs-annotations:${Versions.FIND_BUGS_ANNOTATIONS}")
        compileOnly("org.slf4j:slf4j-api:${Versions.SLF4J}")
        testImplementation("ch.qos.logback:logback-classic:${Versions.LOGBACK}")
    }

    publishing {
        publications {
            register<MavenPublication>("fixtureMonkey") {
                from(components["java"])

                pom {
                    name = "fixture-monkey"
                    description = "The easiest way to generate controllable arbitrary test objects"
                    url = "http://github.com/naver/fixture-monkey"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "ah.jo"
                            name = "SeongAh Jo"
                            email = "ah.jo@navercorp.com"
                        }
                        developer {
                            id = "mhyeon-lee"
                            name = "Myeonghyeon Lee"
                            email = "mheyon.lee@gmail.com"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/naver/fixture-monkey.git"
                        developerConnection = "scm:git:ssh://github.com/naver/fixture-monkey.git"
                        url = "http://github.com/naver/fixture-monkey"
                    }
                }
            }
        }
    }

    signing {
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project

        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

        sign(publishing.publications["fixtureMonkey"])
    }

    tasks.withType<Sign> {
        onlyIf { (version as? String)?.endsWith("SNAPSHOT")?.not() ?: false }
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
            val ossrhUsername: String? by project
            val ossrhPassword: String? by project

            nexusUrl = uri("https://oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            username = if (ossrhUsername != null) ossrhUsername else ""
            password = if (ossrhPassword != null) ossrhPassword else ""
        }
    }
}
