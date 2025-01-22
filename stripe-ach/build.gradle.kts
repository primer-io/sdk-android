@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.stripeach"
}

dependencies {

    compileOnly(libs.primer.stripe)
    testImplementation(libs.primer.stripe)

    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(project(":errors-core"))
    implementation(project(":analytics"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":client-session-actions"))
    implementation(project(":payments-core"))
    implementation(project(":client-token-core"))
    implementation(libs.androidx.activity.ktx)
    testImplementation(libs.mockwebserver)
}
