@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
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
