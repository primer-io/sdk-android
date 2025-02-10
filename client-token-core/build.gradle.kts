plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.clientTokenCore"
}

dependencies {

    implementation(project(":arch-core"))
    implementation(project(":configuration"))
    implementation(project(":errors-core"))
    testImplementation(libs.mockwebserver)
}
