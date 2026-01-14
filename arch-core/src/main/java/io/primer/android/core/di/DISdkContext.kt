package io.primer.android.core.di

import android.util.Log
import androidx.annotation.RestrictTo
import io.primer.android.core.di.exception.SdkContainerUninitializedException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DISdkContext {
    private val merged: SdkContainer by lazy { SdkContainer() }

    data class IntegrationContext(val isDropIn: Boolean, val locale: Locale)

    @Volatile
    var integrationContext = IntegrationContext(isDropIn = false, locale = Locale.getDefault())

    @Volatile
    var dropInSdkContainer: SdkContainer? = null

    @Volatile
    var headlessSdkContainer: SdkContainer? = null

    @Volatile
    var coreContainer: SdkContainer? = null

    val container: () -> SdkContainer
        get() = {
            val selectedContainer =
                if (integrationContext.isDropIn) {
                    dropInSdkContainer + coreContainer
                } else {
                    headlessSdkContainer + coreContainer
                }

            selectedContainer?.let { container ->
                // this is necessary in case we use `getSdkContainer().registerContainer`
                val additionalContainers =
                    merged.containers.filterKeys { key -> container.containers.contains(key).not() }.toMutableMap()
                merged.apply {
                    containers = ConcurrentHashMap(additionalContainers + container.containers.toMutableMap())
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
