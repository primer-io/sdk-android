@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.paymentMethodCoreUi"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(libs.android.material)
    implementation(libs.android.lifecycle.runtime.ktx)
    implementation(libs.android.lifecycle.viewmodel.ktx)

    implementation(project(":payments-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":errors-core"))
}
