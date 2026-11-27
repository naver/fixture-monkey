plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

dependencies {
    testImplementation(projects.fixtureMonkeyKotlin)
}

tasks.test {
    maxHeapSize = "512m"
    jvmArgs("-XX:+UseG1GC")
}
