package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.models.AnalyticsSdkType

internal class AnalyticsSdkTypeResolver {

    fun resolve(): AnalyticsSdkType {
        return if (ReactNativeClassValidator().isReactNativeAvailableOnClassPath()) {
            AnalyticsSdkType.RN_ANDROID
        } else AnalyticsSdkType.ANDROID_NATIVE
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
