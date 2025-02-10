plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.sandboxProcessor"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
    }
}

dependencies {

    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(project(":arch-core"))
    api(project(":api-shared"))
    implementation(project(":configuration"))
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":payments-core"))
}
