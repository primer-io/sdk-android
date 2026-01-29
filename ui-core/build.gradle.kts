plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")


android {
    namespace = "io.primer.android.ui.core"
}

dependencies {

    implementation(project(":headless-core"))
    implementation(project(":client-session-actions"))
    implementation(project(":configuration"))
    implementation(project(":arch-core"))
    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
    implementation(libs.android.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
