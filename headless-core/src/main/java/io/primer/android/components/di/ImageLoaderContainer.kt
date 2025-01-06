package io.primer.android.components.di

import android.content.Context
import io.primer.android.components.utils.ImageLoader
import io.primer.android.configuration.di.ConfigurationCoreContainer.Companion.IMAGE_LOADING_CLIENT_NAME
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

internal class ImageLoaderContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton(IMAGE_LOADING_CLIENT_NAME) {
            Cache(
                File(sdk().resolve<Context>().cacheDir, CACHE_DIRECTORY),
                MAX_CACHE_SIZE_MB,
            )
        }

        registerSingleton(IMAGE_LOADING_CLIENT_NAME) {
            OkHttpClient.Builder().apply {
                cache(resolve(IMAGE_LOADING_CLIENT_NAME))
                addInterceptor(sdk().resolve<HttpLoggerInterceptor>())
            }.build()
        }

        registerFactory { ImageLoader(resolve(IMAGE_LOADING_CLIENT_NAME)) }
    }

    companion object {
        private const val MAX_CACHE_SIZE_MB = 5 * 1024 * 1024L
        private const val CACHE_DIRECTORY = "primer_sdk_image_cache"
    }
}
