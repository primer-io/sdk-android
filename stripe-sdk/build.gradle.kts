plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/wrapper-sdk.gradle")

android {
    namespace = "io.primer.android.stripe"

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(libs.stripe.payments.core) {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }
    implementation(libs.stripe.financial.connections)
    implementation(libs.networking.okhttp)
    implementation(libs.android.appcompat)
}
