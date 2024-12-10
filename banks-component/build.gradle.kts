@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.banks"
}

dependencies {
    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(libs.android.lifecycle.viewmodel.state)

    implementation(project(":errors-core"))
    implementation(project(":arch-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":client-session-actions"))
    implementation(project(":payments-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":client-token-core"))
    implementation(project(":web-redirect-shared"))
}
