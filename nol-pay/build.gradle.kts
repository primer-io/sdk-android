@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply("$rootDir/tooling/android-common.gradle")

android {
    namespace = "io.primer.android.nolpay"

    testOptions {
        unitTests.all { test ->
            // Java 17 reflection quirks
            test.jvmArgs(
                "--add-opens",
                "java.base/java.lang=ALL-UNNAMED",
                "--add-opens",
                "java.base/java.lang.reflect=ALL-UNNAMED",
            )
        }
    }
}

dependencies {
    compileOnly(libs.primer.nol.pay)
    testImplementation(libs.primer.nol.pay)

    implementation(libs.android.lifecycle.viewmodel.state)
    implementation(libs.android.lifecycle.viewmodel.ktx)

    api(project(":analytics"))
    implementation(project(":errors-core"))
    implementation(project(":payment-methods-core"))
    implementation(project(":phone-metadata"))
    implementation(project(":api-shared"))
    implementation(project(":payments-core"))
    implementation(project(":client-token-core"))
}
