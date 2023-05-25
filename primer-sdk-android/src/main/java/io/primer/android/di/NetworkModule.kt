package io.primer.android.di

import io.primer.android.BuildConfig
import io.primer.android.analytics.data.datasource.CheckoutSessionIdDataSource
import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.data.interceptors.HttpAnalyticsInterceptor
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

internal const val SDK_API_VERSION_HEADER = "X-Api-Version"
private const val CONTENT_TYPE_HEADER = "Content-Type"
private const val CONTENT_TYPE_APPLICATION_JSON = "application/json"
private const val SDK_VERSION_HEADER = "Primer-SDK-Version"
private const val SDK_CLIENT_HEADER = "Primer-SDK-Client"
private const val CLIENT_TOKEN_HEADER = "Primer-Client-Token"
private const val PRIMER_SDK_CHECKOUT_SESSION_ID_HEADER = "Primer-SDK-Checkout-Session-ID"

internal val NetworkModule = {
    module {
        single<Interceptor> { HttpAnalyticsInterceptor() }
        single<OkHttpClient> {
            val builder = OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    chain.request().newBuilder()
                        .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                        .addHeader(SDK_VERSION_HEADER, BuildConfig.SDK_VERSION_STRING)
                        .addHeader(SDK_CLIENT_HEADER, SdkTypeResolver().resolve().name)
                        .addHeader(
                            PRIMER_SDK_CHECKOUT_SESSION_ID_HEADER,
                            get<CheckoutSessionIdDataSource>().checkoutSessionId
                        )
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
    }
}

internal enum class ApiVersion(val version: String) {
    CONFIGURATION_VERSION("2.2"),
    PAYMENT_INSTRUMENTS_VERSION("2.2"),
    PAYMENTS_VERSION("2.2"),
    THREE_DS_VERSION("2.1")
}
