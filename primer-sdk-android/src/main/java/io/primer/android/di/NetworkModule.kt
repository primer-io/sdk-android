package io.primer.android.di

import io.primer.android.BuildConfig
import io.primer.android.analytics.data.interceptors.HttpAnalyticsInterceptor
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.model.Model
import io.primer.android.model.Serialization
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

internal const val SDK_API_VERSION_HEADER = "X-Api-Version"
private const val CONTENT_TYPE_HEADER = "Content-Type"
private const val CONTENT_TYPE_APPLICATION_JSON = "application/json"
private const val SDK_VERSION_HEADER = "Primer-SDK-Version"
private const val SDK_CLIENT_HEADER = "Primer-SDK-Client"
private const val SDK_CLIENT_VALUE = "ANDROID_NATIVE"
private const val CLIENT_TOKEN_HEADER = "Primer-Client-Token"

internal val NetworkModule = {
    module {
        single<Interceptor> { HttpAnalyticsInterceptor() }
        single<OkHttpClient> {
            val builder = OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    chain.request().newBuilder()
                        .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                        .addHeader(SDK_VERSION_HEADER, BuildConfig.SDK_VERSION_STRING)
                        .addHeader(SDK_CLIENT_HEADER, SDK_CLIENT_VALUE)
                        .addHeader(
                            CLIENT_TOKEN_HEADER,
                            get<LocalClientTokenDataSource>().get().accessToken
                        )
                        .build()
                        .let { chain.proceed(it) }
                }
            if (BuildConfig.DEBUG) {
                builder.addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
            builder.addInterceptor(get())
                .build()
        }
        single { Serialization.json }
        single {
            Model(
                get(),
                get(),
                get(),
            )
        }
    }
}

internal enum class ApiVersion(val version: String) {
    CONFIGURATION_VERSION("2.1"),
    PAYMENT_INSTRUMENTS_VERSION("2.1"),
    PAYMENTS_VERSION("2021-09-27"),
    THREE_DS_VERSION("2021-12-10")
}
