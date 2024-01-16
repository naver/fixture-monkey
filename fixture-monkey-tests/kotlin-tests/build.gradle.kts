plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}")

    testImplementation(project(":fixture-monkey-kotlin"))
    testImplementation(project(":fixture-monkey-kotest"))
    testImplementation(project(":fixture-monkey-javax-validation"))
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
    testImplementation("io.kotest:kotest-assertions-core:${Versions.KOTEST}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}
