plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT
}

dependencies {
    api(project(":fixture-monkey-kotlin"))
    api("io.kotest:kotest-property-jvm:${Versions.KOTEST}")

    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}")
}
