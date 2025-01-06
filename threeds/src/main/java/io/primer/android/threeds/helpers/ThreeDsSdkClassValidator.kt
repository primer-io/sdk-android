package io.primer.android.threeds.helpers

internal class ThreeDsSdkClassValidator {
    fun is3dsSdkIncluded(): Boolean {
        return try {
            Class.forName(THREE_DS_CLASS_NAME)
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

    companion object {
        const val THREE_DS_CLASS_NAME = "com.netcetera.threeds.sdk.ThreeDS2ServiceInstance"
        const val THREE_DS_CLASS_NOT_LOADED_ERROR =
            "Primer 3ds-android library not found on the " +
                "classpath. Please follow the 3DS integration guide."
    }
}
