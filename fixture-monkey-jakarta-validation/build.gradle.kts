plugins {
    id("com.navercorp.fixturemonkey.gradle.plugin.java-conventions")
}

dependencies {
    api(project(":fixture-monkey-api"))
    api("org.hibernate.validator:hibernate-validator:7.0.5.Final")
    api("jakarta.validation:jakarta.validation-api:3.0.2")
    api("org.glassfish:jakarta.el:4.0.2")

    testImplementation(project(":fixture-monkey"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}")
    testImplementation("org.junit.platform:junit-platform-engine:${Versions.JUNIT_ENGINE}")
    testImplementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    testImplementation("org.projectlombok:lombok:${Versions.LOMBOK}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.LOMBOK}")
}


