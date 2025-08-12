plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

apply("$rootDir/tooling/android-common.gradle")


android {
    namespace = "io.primer.android.components"
    compileSdk = 35


    defaultConfig {
        minSdk = 21

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
}

dependencies {

    implementation(project(":headless-core"))
    api(project(":ui-core"))
    implementation(project(":payment-card-shared"))
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.material3)
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
}
