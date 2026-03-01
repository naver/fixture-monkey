plugins { `kotlin-dsl` }

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.2.5")
    implementation("org.checkerframework:checkerframework-gradle-plugin:0.6.61")
}

gradlePlugin {
    plugins {
        register("optionalDependenciesPlugin") {
            id = "com.navercorp.fixturemonkey.gradle.plugin.optional-dependencies"
            implementationClass = "com.navercorp.fixturemonkey.gradle.plugin.OptionalDependenciesPlugin"
        }
        register("javaConventionsPlugin") {
            id = "com.navercorp.fixturemonkey.gradle.plugin.java-conventions"
            implementationClass = "com.navercorp.fixturemonkey.gradle.plugin.JavaConventionsPlugin"
        }
        register("mavenPublishConventionsPlugin") {
            id = "com.navercorp.fixturemonkey.gradle.plugin.maven-publish-conventions"
            implementationClass = "com.navercorp.fixturemonkey.gradle.plugin.MavenPublishConventionsPlugin"
        }
        register("objectFarmMavenPublishConventionsPlugin") {
            id = "com.navercorp.fixturemonkey.gradle.plugin.object-farm-maven-publish-conventions"
            implementationClass = "com.navercorp.fixturemonkey.gradle.plugin.ObjectFarmMavenPublishConventionsPlugin"
        }
        register("checkerFrameworkConventionsPlugin") {
            id = "com.navercorp.fixturemonkey.gradle.plugin.checker-framework-conventions"
            implementationClass = "com.navercorp.fixturemonkey.gradle.plugin.CheckerFrameworkConventionsPlugin"
        }
    }
}
