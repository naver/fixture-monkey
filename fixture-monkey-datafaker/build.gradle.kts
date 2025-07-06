plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey-api"))
    implementation("net.datafaker:datafaker:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")

    testImplementation(project(":fixture-monkey"))
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    implementation(kotlin("stdlib-jdk8"))
}
