plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.object-farm-maven-publish-conventions")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

configurations.compileOnly {
    exclude(group = "org.junit.platform", module = "junit-platform-commons")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

