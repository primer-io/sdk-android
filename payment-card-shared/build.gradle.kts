plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.cardShared"
}

dependencies {

    implementation(project(":configuration"))
    implementation(project(":payment-methods-core"))
    testImplementation(libs.mockwebserver)
}
