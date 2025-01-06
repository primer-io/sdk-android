package io.primer.android.core.di

import android.util.Log
import androidx.annotation.RestrictTo
import io.primer.android.core.di.exception.SdkContainerUninitializedException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DISdkContext {
    private val merged: SdkContainer by lazy { SdkContainer() }

    var isDropIn: Boolean = false
    var dropInSdkContainer: SdkContainer? = null
    var headlessSdkContainer: SdkContainer? = null
    var coreContainer: SdkContainer? = null

    val container: () -> SdkContainer
        get() = {
            val selectedContainer =
                if (isDropIn) {
                    dropInSdkContainer + coreContainer
                } else {
                    headlessSdkContainer + coreContainer
                }

            selectedContainer?.let { container ->
                // this is necessary in case we use `getSdkContainer().registerContainer`
                val additionalContainers =
                    merged.containers.filterKeys { key -> container.containers.contains(key).not() }.toMutableMap()
                merged.apply {
                    containers = (additionalContainers + container.containers).toMutableMap()
                }
            }?.takeUnless { container -> container.containers.isEmpty() }
                ?: throw SdkContainerUninitializedException()
        }

    fun getContainerOrNull(): SdkContainer? =
        runCatching { container() }.getOrNull() ?: run {
            Log.e("DISdkContextKt", "Container is not initialized")
            null
        }

    fun clear() = merged.clear()
}
