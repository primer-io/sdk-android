@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

ext.set("mavenArtifactDescription", "Primer Architecture Core Android SDK")

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.core"

    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

dependencies {
    api(libs.networking.okhttp)
    api(libs.kotlin.stdlib)
    api(libs.kotlin.coroutines)
    api(libs.android.ktx)
    compileOnly(libs.android.lifecycle.viewmodel.ktx)
}
