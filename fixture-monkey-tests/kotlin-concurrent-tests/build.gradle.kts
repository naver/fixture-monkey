plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
}

dependencies {
    testImplementation(project(":fixture-monkey-kotlin"))
}
