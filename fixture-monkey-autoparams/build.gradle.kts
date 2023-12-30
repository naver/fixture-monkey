plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    api(project(":fixture-monkey"))
    api("io.github.autoparams:autoparams:${Versions.AUTO_PARAMS}")
    compileOnly(project(":fixture-monkey-engine"))

    testImplementation(project(":fixture-monkey-engine"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
