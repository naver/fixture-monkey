plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    api("net.jqwik:jqwik-engine:${Versions.JQWIK}")
    implementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    implementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")

    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}
