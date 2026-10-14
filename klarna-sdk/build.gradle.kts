plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://x.klarnacdn.net/mobile-sdk/")
    }
}

apply("$rootDir/tooling/wrapper-sdk.gradle")

android {
    namespace = "io.primer.android.klarna"
}

dependencies {
    api(libs.klarna)
}
