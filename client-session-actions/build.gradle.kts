plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.clientSessionActions"
}

dependencies {
    implementation(project(":arch-core"))
    implementation(project(":errors-core"))
    implementation(project(":configuration"))
    implementation(project(":api-shared"))
    testImplementation(libs.mockwebserver)
}
