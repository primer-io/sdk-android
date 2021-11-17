package io.primer.android.di

import io.primer.android.utils.ImageLoader
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val IMAGE_LOADING_CLIENT_NAME = "IMAGE_LOADING_CLIENT"

internal val imageLoaderModule = {
    module {
        single(named(IMAGE_LOADING_CLIENT_NAME)) { OkHttpClient() }
        factory { ImageLoader(get(named(IMAGE_LOADING_CLIENT_NAME))) }
    }
}
