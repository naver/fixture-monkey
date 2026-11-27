plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

tasks.test {
    maxHeapSize = "512m"
    jvmArgs("-XX:+UseG1GC")
}
