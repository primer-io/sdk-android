plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/wrapper-sdk.gradle")

android {
    // The vendored iPay88 classes look up their R class under their own package, so the namespace stays theirs.
    namespace = "com.androids.ipay"

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

// libs/classes.jar already ships com.androids.ipay.BuildConfig; generating another one would duplicate it.
tasks.configureEach {
    if (name.startsWith("generate") && name.endsWith("BuildConfig")) {
        enabled = false
    }
}

dependencies {
    // api: the glue module compiles against the vendor classes, as it did against the published AAR.
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.android.appcompat)
}
