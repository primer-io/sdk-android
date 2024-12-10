@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.threeds.main"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
    }
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(libs.android.lifecycle.viewmodel.ktx)

    compileOnly(libs.primer.threeds)
    testImplementation(libs.primer.threeds)

    implementation(project(":payments-core"))
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core-ui"))
    implementation(project(":configuration"))

    testImplementation(project(":arch-core"))
}
