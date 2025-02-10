plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.ipay88"
}

dependencies {

    compileOnly(libs.primer.ipay88)
    testImplementation(libs.primer.ipay88)

    implementation(libs.android.appcompat)

    api(project(":analytics"))
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":payments-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":client-token-core"))
}
