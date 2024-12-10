@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.bancontact"
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(project(":payment-methods-core"))
    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
    implementation(project(":client-token-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":payment-card-shared"))
    implementation(project((":web-redirect-shared")))
}
