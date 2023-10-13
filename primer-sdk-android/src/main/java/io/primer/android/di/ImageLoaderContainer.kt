package io.primer.android.di

import android.content.Context
import io.primer.android.utils.ImageLoader
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

internal class ImageLoaderContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton {
            Cache(
                File((sdk.resolve() as Context).cacheDir, CACHE_DIRECTORY),
                MAX_CACHE_SIZE_MB
            )
        }

        registerSingleton(IMAGE_LOADING_CLIENT_NAME) {
            OkHttpClient.Builder().cache(resolve()).build()
        }

        registerFactory { ImageLoader(resolve(IMAGE_LOADING_CLIENT_NAME)) }
    }

    companion object {
        const val IMAGE_LOADING_CLIENT_NAME = "IMAGE_LOADING_CLIENT"
        private const val MAX_CACHE_SIZE_MB = 5 * 1024 * 1024L
        private const val CACHE_DIRECTORY = "primer_sdk_image_cache"
    }
}
