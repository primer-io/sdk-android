plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.statetransport"
}

dependencies {

    implementation(project(":arch-core"))
    implementation(project(":configuration"))

    implementation(libs.kotlin.coroutines)
}
