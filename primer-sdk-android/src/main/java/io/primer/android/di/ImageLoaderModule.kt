package io.primer.android.di

import android.content.Context
import io.primer.android.utils.ImageLoader
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

internal const val IMAGE_LOADING_CLIENT_NAME = "IMAGE_LOADING_CLIENT"
private const val MAX_CACHE_SIZE_MB = 5 * 1024 * 1024L
private const val CACHE_DIRECTORY = "primer_sdk_image_cache"

internal val imageLoaderModule = {
    module {
        single { Cache(File(get<Context>().cacheDir, CACHE_DIRECTORY), MAX_CACHE_SIZE_MB) }
        single(named(IMAGE_LOADING_CLIENT_NAME)) { OkHttpClient.Builder().cache(get()).build() }
        factory { ImageLoader(get(named(IMAGE_LOADING_CLIENT_NAME))) }
    }
}
