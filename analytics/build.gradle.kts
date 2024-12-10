@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.analytics"
}

dependencies {

    implementation(project(":arch-core"))
    implementation(project(":logging"))

    implementation(libs.kotlin.coroutines)
    implementation(libs.android.lifecycle.runtime.ktx)
    implementation(libs.android.startup)
    implementation(libs.androidx.lifecycle.process)
}
