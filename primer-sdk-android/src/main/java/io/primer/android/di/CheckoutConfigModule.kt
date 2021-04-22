package io.primer.android.di

import io.primer.android.BuildConfig
import io.primer.android.PaymentMethod
import io.primer.android.UniversalCheckoutTheme
import io.primer.android.model.Model
import io.primer.android.model.UniversalJson
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientToken
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

internal val CheckoutConfigModule = { config: CheckoutConfig, paymentMethods: List<PaymentMethod> ->
    module {
        single<CheckoutConfig> { config }
        single<List<PaymentMethod>> { paymentMethods }
        single<UniversalCheckoutTheme> { config.theme }
        single<ClientToken> { ClientToken.fromString(get<CheckoutConfig>().clientToken) }
        single<OkHttpClient> {
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level =
                            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                            else HttpLoggingInterceptor.Level.NONE
                    }
                )
                .addInterceptor { chain: Interceptor.Chain ->
                    chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Primer-SDK-Version", BuildConfig.SDK_VERSION_STRING)
                        .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
                        .addHeader("Primer-Client-Token", get<ClientToken>().accessToken)
                        .build()
                        .let { chain.proceed(it) }
                }
                .build()
        }
        single<Json> { UniversalJson.json }
        single<Model> {
            Model(
                clientToken = get(),
                config = get(),
                okHttpClient = get(),
                json = get()
            )
        }
    }
}
