plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/wrapper-sdk.gradle")

// Repackaged Netcetera 3DS SDK: no sources of our own, only the vendored jar, native libs, assets and resources.
android {
    namespace = "io.primer.android.threeds"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "consumer-rules.pro")
        }
    }
}

dependencies {
    // api: the glue module compiles against the Netcetera classes, as it did against the published AAR.
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.android.appcompat)
    implementation(libs.slf4j.api)
    implementation(libs.logback.android)
    implementation(libs.play.services.base)
}
