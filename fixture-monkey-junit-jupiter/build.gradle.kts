plugins {
    id("org.jetbrains.kotlin.jvm") 
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey"))
    api("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT_JUPITER}")

    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
