plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.paymentsCore"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
    }
}

dependencies {

    implementation(project(":logging"))
    implementation(project(":arch-core"))
    api(project(":api-shared"))
    implementation(project(":configuration"))
    implementation(project(":errors-core"))
    testImplementation(libs.mockwebserver)
}
