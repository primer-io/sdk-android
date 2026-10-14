plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.ipay88"
}

dependencies {

    compileOnly(project(":ipay88-sdk"))
    testImplementation(project(":ipay88-sdk"))

    implementation(libs.android.appcompat)

    api(project(":analytics"))
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":payments-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":client-token-core"))
}
