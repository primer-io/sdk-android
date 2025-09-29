package io.primer.android.core.utils

/**
 * Resolves the SDK type based on the runtime environment.
 * Detects whether the SDK is running in a native Android app or React Native environment.
 */
object SdkTypeResolver {

    private const val RN_CLASS_NAME = "com.facebook.react.ReactActivity"

    /**
     * Determines the current SDK type by checking the classpath for React Native classes.
     * @return The detected SDK type
     */
    fun resolve(): SdkType {
        return if (isReactNativeAvailable()) {
            SdkType.RN_ANDROID
        } else {
            SdkType.ANDROID_NATIVE
        }
    }

    private fun isReactNativeAvailable(): Boolean {
        return try {
            Class.forName(RN_CLASS_NAME)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }
}

/**
 * Enum representing the different SDK types
 */
enum class SdkType {
    /**
     * Native Android SDK
     */
    ANDROID_NATIVE,

    /**
     * React Native Android SDK
     */
    RN_ANDROID,
}
