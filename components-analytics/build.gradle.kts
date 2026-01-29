plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.components.analytics"
}

dependencies {
    implementation(project(":arch-core"))
    implementation(project(":configuration"))
    implementation(project(":api-shared"))
    implementation(project(":logging"))
    implementation(project(":analytics"))

    implementation(libs.kotlin.coroutines)
    implementation(libs.android.lifecycle.runtime.ktx)
    
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
}
