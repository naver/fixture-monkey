plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint") version Versions.KTLINT
    id("com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions")
}

dependencies {
    api(project(":fixture-monkey-kotlin"))
    api("io.kotest:kotest-property-jvm:${Versions.KOTEST}")

    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}")
}
