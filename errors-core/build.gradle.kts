plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.errors"
}

dependencies {
    api(project(":analytics"))
    implementation(project(":api-shared"))
    implementation(project(":configuration"))
}
