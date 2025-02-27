plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.paypal"
}

dependencies {
    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)

    implementation(project(":arch-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":errors-core"))
    implementation(project(":api-shared"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":payments-core"))
    implementation(project(":custom-tabs"))
    testImplementation(libs.mockwebserver)
}
