package io.primer.android

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class HttpClientFactory(
    private val accessToken: String,
    private val sdkVersion: String,
) {
    fun build(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Primer-SDK-Version", sdkVersion)
                    .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
                    .addHeader("Primer-Client-Token", accessToken)
                    .build()
                    .let { chain.proceed(it) }
            }
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }
}
