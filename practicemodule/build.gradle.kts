plugins {
    id("java")
}

group = "com.navercorp.fixturemonkey"
version = "0.6.2-SNASPHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}