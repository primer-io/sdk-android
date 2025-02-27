plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.offsession"
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(project(":arch-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
    implementation(project(":client-token-core"))
    implementation(project(":phone-metadata"))
    testImplementation(libs.mockwebserver)
}
