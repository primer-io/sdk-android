plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

ext.set("mavenArtifactDescription", "Primer Architecture Core Android SDK")

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.core"

    testFixtures {
        enable = true
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

dependencies {
    api(libs.networking.okhttp)
    api(libs.kotlin.stdlib)
    api(libs.kotlin.coroutines)
    api(libs.android.ktx)
    compileOnly(libs.android.lifecycle.viewmodel.ktx)

    testFixturesRuntimeOnly(libs.junit.jupiter.api)
    testFixturesImplementation(libs.arch.core)
    testFixturesImplementation(libs.junit.jupiter.engine)
    testFixturesImplementation(libs.junit.jupiter.params)
    testFixturesImplementation(libs.kotlin.test.junit)
    testFixturesImplementation(libs.kotlin.coroutines.test)
}
