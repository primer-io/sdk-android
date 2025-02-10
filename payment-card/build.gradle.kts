plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.card"
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)

    api(project(":payment-card-shared"))
    api(project(":threeds"))

    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
    implementation(project(":client-token-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":processor-3ds"))
}
