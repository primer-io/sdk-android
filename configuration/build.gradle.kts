plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.configuration"
}

dependencies {

    api(project(":arch-core"))
    api(project(":logging"))
    api(project(":analytics"))
    testImplementation(libs.mockwebserver)
}
