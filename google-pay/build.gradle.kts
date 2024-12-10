@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.googlepay"
}

dependencies {

    implementation(libs.play.services.wallet)
    implementation(project(":payment-methods-core"))
    implementation(project(":errors-core"))
    implementation(project(":api-shared"))
    implementation(project(":payments-core"))
    implementation(project(":client-session-actions"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":client-token-core"))
    implementation(project(":threeds"))
    implementation(project(":processor-3ds"))
}
