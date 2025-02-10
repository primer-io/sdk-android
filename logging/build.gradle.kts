plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.core.logging"
}

dependencies {
    implementation(libs.networking.okhttp)
    implementation(libs.kotlin.stdlib)
    implementation(libs.android.ktx)
    implementation(project(":arch-core"))
}
