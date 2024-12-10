@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.webRedirectShared"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":arch-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":errors-core"))
    implementation(project(":payments-core"))
}
