@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.processor3ds"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
    }
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":arch-core"))
    implementation(project(":analytics"))
    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
}
