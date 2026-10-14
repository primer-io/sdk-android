plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/wrapper-sdk.gradle")

android {
    // The vendored Transit SDK reads its BuildConfig and resources from its own package, so the namespace stays theirs.
    namespace = "com.snowballtech.transit.rta"

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "consumer-rules.pro",
            )
        }
        // The Transit SDK reads SDK_VERSION_STRING as its own version; the wrapper version lives next to it.
        configureEach {
            buildConfigField("String", "SDK_VERSION_STRING", "\"3.1.11\"")
            buildConfigField("String", "SDK_WRAPPER_VERSION_STRING", "\"${project.findProperty("VERSION_NAME")}\"")
        }
    }
}

dependencies {
    // api: the glue module compiles against the vendor classes, as it did against the published AAR.
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.security.crypto)
    implementation(libs.spongycastle.core)
    implementation(libs.spongycastle.prov)
    implementation(libs.spongycastle.bcpkix)
    implementation(libs.gson)
}
