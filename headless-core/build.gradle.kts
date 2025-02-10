plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.headlessCore"
    kotlinOptions {
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(libs.android.material)
    implementation(project(":configuration"))
    implementation(project(":paypal"))
    api(project(":google-pay"))
    implementation(project(":web-redirect"))
    implementation(project(":ipay88"))
    implementation(project(":web-redirect-shared"))

    api(project(":generic-off-session"))
    api(project(":payment-methods-core-ui"))
    api(project(":payments-core"))
    api(project(":client-token-core"))
    api(project(":client-session-actions"))
    api(project(":payment-methods-core"))
    api(project(":errors-core"))
    api(project(":api-shared"))
    api(project(":banks-component"))
    api(project(":stripe-ach"))
    api(project(":payment-card"))
    api(project(":headless-vault-manager"))
    api(project(":nol-pay"))
    api(project(":klarna"))
    api(project(":bancontact-card"))
    api(project(":primer-sandbox-processor"))
}
