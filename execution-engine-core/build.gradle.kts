plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")


android {
    namespace = "io.primer.executionengine"
}

dependencies {

    implementation(project(":arch-core"))
    implementation(project(":logging"))
    implementation(project(":analytics"))

    implementation(libs.kotlin.coroutines)

    testImplementation(libs.mockwebserver)
}
