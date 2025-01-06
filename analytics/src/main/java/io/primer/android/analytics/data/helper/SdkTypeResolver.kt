package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.models.SdkType

class SdkTypeResolver {
    fun resolve(): SdkType {
        return if (ReactNativeClassValidator().isReactNativeAvailableOnClassPath()) {
            SdkType.RN_ANDROID
        } else {
            SdkType.ANDROID_NATIVE
        }
    }
}

internal class ReactNativeClassValidator {
    fun isReactNativeAvailableOnClassPath(): Boolean {
        return try {
            Class.forName(RN_CLASS_NAME)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

    companion object {
        private const val RN_CLASS_NAME = "com.facebook.react.ReactActivity"
    }
}
