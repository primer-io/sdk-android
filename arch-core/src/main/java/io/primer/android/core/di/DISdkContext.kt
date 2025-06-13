package io.primer.android.core.di

import android.util.Log
import androidx.annotation.RestrictTo
import io.primer.android.core.di.exception.SdkContainerUninitializedException
import java.util.concurrent.ConcurrentHashMap

enum class SdkType {
    HEADLESS,
    DROP_IN,
    COMPONENTS
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DISdkContext {
    private val merged: SdkContainer by lazy { SdkContainer() }

    var sdkType: SdkType = SdkType.HEADLESS
    @Volatile
    var dropInSdkContainer: SdkContainer? = null

    @Volatile
    var headlessSdkContainer: SdkContainer? = null
  
    @Volatile
    var componentsSdkContainer: SdkContainer? = null

    @Volatile
    var coreContainer: SdkContainer? = null

    val container: () -> SdkContainer
        get() = {
            val selectedContainer = when (sdkType) {
                SdkType.DROP_IN -> dropInSdkContainer + coreContainer
                SdkType.HEADLESS -> headlessSdkContainer + coreContainer
                SdkType.COMPONENTS -> componentsSdkContainer + coreContainer
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
