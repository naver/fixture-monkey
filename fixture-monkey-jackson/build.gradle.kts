plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    api(project(":fixture-monkey"))
    api("com.fasterxml.jackson.core:jackson-databind:${Versions.JACKSON}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Versions.JACKSON}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.JACKSON}")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


