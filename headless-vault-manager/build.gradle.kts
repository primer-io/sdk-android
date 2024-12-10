@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.vaultManager"
}

dependencies {

    implementation(project(":arch-core"))
    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
    implementation(project(":configuration"))
    implementation(project(":payment-methods-core"))
    implementation(project(":payment-methods-core-ui"))
}
