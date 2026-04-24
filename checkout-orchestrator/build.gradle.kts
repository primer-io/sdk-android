plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.checkout.orchestrator"
}

dependencies {
    implementation(libs.android.appcompat)
    implementation(project(":arch-core"))
    implementation(project(":configuration"))
    implementation(project(":errors-core"))
    implementation(project(":js-core"))
    implementation(project(":execution-engine-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":payments-core"))
    implementation(project(":state-transport"))
    implementation(project(":web-redirect-shared"))

    implementation(libs.kotlin.coroutines)
}
