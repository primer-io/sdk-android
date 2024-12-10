@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.paymentMethodsCore"
}

dependencies {
    api(project(":configuration"))
    implementation(project(":api-shared"))
    implementation(project(":errors-core"))
    implementation(project(":payments-core"))
    implementation(project(":client-token-core"))
}
