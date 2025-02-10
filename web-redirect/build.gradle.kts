plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.webredirect"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
    }
}

dependencies {

    implementation(libs.android.appcompat)
    implementation(project(":payment-methods-core"))
    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":client-token-core"))
    implementation(project(":web-redirect-shared"))
}
