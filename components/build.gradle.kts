plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

apply("$rootDir/tooling/android-common.gradle")

// Remove JUnit5 runner builder from android-common.gradle - Compose UI tests use JUnit4
the<com.android.build.gradle.LibraryExtension>().defaultConfig.testInstrumentationRunnerArguments.remove("runnerBuilder")

android {
    namespace = "io.primer.android.components"
    compileSdk = 36


    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
            )
        }
    }

    // Disable test orchestrator from android-common.gradle for simpler Compose UI tests
    testOptions {
        execution = "HOST"
    }
}

dependencies {

    implementation(project(":headless-core"))
    implementation(project(":ui-core"))
    implementation(project(":payment-card-shared"))
    implementation(project(":components-analytics"))
    implementation(project(":klarna"))
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Compose Preview support
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Android Test dependencies (JUnit4 for Compose UI testing)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation("junit:junit:4.13.2")
}
