package io.primer.android.di

import io.primer.android.BuildConfig
import io.primer.android.PaymentMethod
import io.primer.android.data.session.datasource.LocalClientSessionDataSource
import io.primer.android.events.EventDispatcher
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource
import io.primer.android.model.Model
import io.primer.android.model.Serialization
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientToken
import io.primer.android.threeds.data.repository.PaymentMethodDataRepository
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

internal val CheckoutConfigModule = { config: CheckoutConfig, paymentMethods: List<PaymentMethod> ->
    module {
        single { config }
        single { paymentMethods }
        single { config.theme }
        single { ClientToken.fromString(get<CheckoutConfig>().clientToken) }
        single<OkHttpClient> {
            OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Primer-SDK-Version", BuildConfig.SDK_VERSION_STRING)
                        .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
                        .addHeader("Primer-Client-Token", get<ClientToken>().accessToken)
                        .build()
                        .let { chain.proceed(it) }
                }.addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level =
                            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                            else HttpLoggingInterceptor.Level.NONE
                    }
                )
                .build()
        }
        single { Serialization.json }
        single {
            Model(
                get(),
                get(),
                get(),
                get()
            )
        }

        single {
            LocalClientSessionDataSource()
        }
        single<PaymentMethodRepository> { PaymentMethodDataRepository() }

        single {
            EventDispatcher()
        }

        single {
            MetaDataSource(
                get(),
            )
        }
    }
}
