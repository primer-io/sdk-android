plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.jscore"
}


dependencies {

    implementation(project(":arch-core"))
    implementation(project(":logging"))

    implementation(libs.androidx.javascriptengine)
    implementation(libs.kotlin.coroutines)
}
