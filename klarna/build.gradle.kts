plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://x.klarnacdn.net/mobile-sdk/")
    }
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.klarna.main"
}

dependencies {
    compileOnly(libs.primer.klarna)
    testImplementation(libs.primer.klarna)

    api(project(":analytics"))

    implementation(libs.android.lifecycle.viewmodel.ktx)
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":api-shared"))
    implementation(project(":client-session-actions"))
    implementation(project(":payments-core"))
    testImplementation(libs.mockwebserver)
}
