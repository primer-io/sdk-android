package io.primer.android.di

import android.content.Context
import io.primer.android.BuildConfig
import io.primer.android.analytics.data.datasource.CheckoutSessionIdDataSource
import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.data.interceptors.HttpAnalyticsInterceptor
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoDataRequest
import io.primer.android.components.data.payments.paymentMethods.nolpay.model.NolPaySecretDataRequest
import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.BlacklistedHttpHeadersProvider
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.data.rpc.banks.models.IssuingBankDataRequest
import io.primer.android.data.rpc.banks.models.IssuingBankResultDataResponse
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.http.PrimerHttpClient
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File

internal class NetworkContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { HttpAnalyticsInterceptor() }

        registerSingleton {
            HttpLoggerInterceptor(
                logReporter = sdk.resolve(),
                blacklistedHttpHeaderProviderRegistry =
                sdk.resolve<BlacklistedHttpHeaderProviderRegistry>().apply {
                    register(
                        sdk.resolve<BlacklistedHttpHeadersProvider>(
                            HttpLogObfuscationContainer.DEFAULT_NAME
                        )
                    )
                },
                whitelistedHttpBodyKeyProviderRegistry =
                sdk.resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
                    listOf(
                        CreateSessionDataRequest.provider,
                        CreateSessionDataResponse.provider,
                        CreateCustomerTokenDataRequest.provider,
                        CreateCustomerTokenDataResponse.provider,
                        PaypalCreateOrderDataRequest.provider,
                        PaypalOrderInfoDataRequest.provider,
                        PaypalCreateBillingAgreementDataRequest.provider,
                        PaypalConfirmBillingAgreementDataRequest.provider,
                        IssuingBankDataRequest.provider,
                        IssuingBankResultDataResponse.provider,
                        NolPaySecretDataRequest.provider,
                        ConfigurationDataResponse.provider
                    ).forEach(::register)
                },
                localConfigurationDataSource = sdk.resolve()
            )
        }

        registerSingleton {
            Cache(
                File(sdk.resolve<Context>().cacheDir, CACHE_DIRECTORY),
                MAX_CACHE_SIZE_MB
            )
        }

        registerSingleton {
            buildOkhttpClient(
                sdk.resolve(),
                sdk.resolve(),
                resolve()
            )
        }

        registerSingleton { PrimerHttpClient(resolve()) }
    }

    private fun buildOkhttpClient(
        checkoutSessionIdDataSource: CheckoutSessionIdDataSource,
        localClientTokenDataSource: LocalClientTokenDataSource,
        cache: Cache
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain: Interceptor.Chain ->
                chain.request().newBuilder()
                    .addHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                    .addHeader(SDK_VERSION_HEADER, BuildConfig.SDK_VERSION_STRING)
                    .addHeader(SDK_CLIENT_HEADER, SdkTypeResolver().resolve().name)
                    .addHeader(
                        PRIMER_SDK_CHECKOUT_SESSION_ID_HEADER,
                        checkoutSessionIdDataSource.checkoutSessionId
                    )
                    .addHeader(
                        CLIENT_TOKEN_HEADER,
                        localClientTokenDataSource.get().accessToken
                    )
                    .build()
                    .let { chain.proceed(it) }
            }
        builder.addInterceptor(resolve<HttpAnalyticsInterceptor>())
        builder.addInterceptor(resolve<HttpLoggerInterceptor>())
        return builder.build()
    }

    companion object {
        internal const val SDK_API_VERSION_HEADER = "X-Api-Version"
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val CONTENT_TYPE_APPLICATION_JSON = "application/json"
        private const val SDK_VERSION_HEADER = "Primer-SDK-Version"
        private const val SDK_CLIENT_HEADER = "Primer-SDK-Client"
        private const val CLIENT_TOKEN_HEADER = "Primer-Client-Token"
        private const val PRIMER_SDK_CHECKOUT_SESSION_ID_HEADER = "Primer-SDK-Checkout-Session-ID"
        private const val MAX_CACHE_SIZE_MB = 5 * 1024 * 1024L
        private const val CACHE_DIRECTORY = "primer_sdk_cache"
    }
}

internal enum class ApiVersion(val version: String) {
    CONFIGURATION_VERSION("2.2"),
    PAYMENT_INSTRUMENTS_VERSION("2.2"),
    PAYMENTS_VERSION("2.2"),
    THREE_DS_VERSION("2.1")
}
