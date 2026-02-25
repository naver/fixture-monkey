pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "fixture-monkey-parent"

include(
        "object-farm-api",
        "fixture-monkey-api",
        "fixture-monkey-engine",
        "fixture-monkey",
        "fixture-monkey-javax-validation",
        "fixture-monkey-kotlin",
        "fixture-monkey-jackson",
        "fixture-monkey-jackson3",
        "fixture-monkey-autoparams",
        "fixture-monkey-mockito",
        "fixture-monkey-starter",
        "fixture-monkey-starter-kotlin",
        "fixture-monkey-junit-jupiter",
        "fixture-monkey-jakarta-validation",
        "fixture-monkey-tests",
        "fixture-monkey-tests:java-17-tests",
        "fixture-monkey-tests:kotlin-tests",
        "fixture-monkey-tests:java-tests",
        "fixture-monkey-tests:java-concurrent-tests",
        "fixture-monkey-tests:kotlin-concurrent-tests",
        "fixture-monkey-kotest",
        "fixture-monkey-benchmarks",
        "fixture-monkey-benchmarks:fixture-monkey-benchmark",
        "fixture-monkey-benchmarks:fixture-monkey-benchmark-kotlin",
        "fixture-monkey-datafaker",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
