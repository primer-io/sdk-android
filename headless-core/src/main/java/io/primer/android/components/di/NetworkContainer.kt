package io.primer.android.components.di

import android.content.Context
import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.analytics.di.AnalyticsContainer.Companion.MESSAGE_LOG_PROVIDER_DI_KEY
import io.primer.android.analytics.di.AnalyticsContainer.Companion.MESSAGE_PROPERTIES_PROVIDER_DI_KEY
import io.primer.android.clientToken.core.token.data.datasource.CacheClientTokenDataSource
import io.primer.android.clientToken.di.ClientTokenCoreContainer
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.BuildConfig
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.logging.di.HttpLogObfuscationContainer
import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

internal class NetworkContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton {
            HttpLoggerInterceptor(
                logReporter = sdk().resolve(),
                blacklistedHttpHeaderProviderRegistry =
                sdk().resolve<BlacklistedHttpHeaderProviderRegistry>()
                    .apply {
                        register(
                            sdk().resolve(
                                HttpLogObfuscationContainer.DEFAULT_NAME
                            )
                        )
                    },
                whitelistedHttpBodyKeyProviderRegistry =
                sdk().resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
                    listOf(
                        ConfigurationDataResponse.provider
                    ).forEach(::register)
                },
                pciUrlProvider = {
                    runCatching {
                        sdk().resolve<CacheConfigurationDataSource>(
                            ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY
                        ).get().pciUrl
                    }.getOrNull()
                }
            )
        }

        registerSingleton {
            Cache(
                File(sdk().resolve<Context>().cacheDir, CACHE_DIRECTORY),
                MAX_CACHE_SIZE_MB
            )
        }

        registerSingleton {
            buildOkhttpClient(
                sdk().resolve(AnalyticsContainer.CHECKOUT_SESSION_ID_PROVIDER_DI_KEY),
                sdk().resolve(ClientTokenCoreContainer.CACHE_CLIENT_TOKEN_DATA_SOURCE_DI_KEY),
                resolve()
            )
        }

        registerSingleton {
            PrimerHttpClient(
                okHttpClient = resolve(),
                logProvider = sdk().resolve(MESSAGE_LOG_PROVIDER_DI_KEY),
                messagePropertiesEventProvider = sdk().resolve(MESSAGE_PROPERTIES_PROVIDER_DI_KEY)
            )
        }
    }

    private fun buildOkhttpClient(
        checkoutSessionIdProvider: CheckoutSessionIdProvider,
        localClientTokenDataSource: CacheClientTokenDataSource,
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
                        checkoutSessionIdProvider.provide()
                    )
                    .addHeader(
                        CLIENT_TOKEN_HEADER,
                        localClientTokenDataSource.get().accessToken
                    )
                    .build()
                    .let { chain.proceed(it) }
            }
        builder.addInterceptor(sdk().resolve(AnalyticsContainer.HTTP_INTERCEPTOR_DI_KEY) as Interceptor)
        builder.addInterceptor(resolve<HttpLoggerInterceptor>())
        builder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        builder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        return builder.build()
    }

    companion object {
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val CONTENT_TYPE_APPLICATION_JSON = "application/json"
        private const val SDK_VERSION_HEADER = "Primer-SDK-Version"
        private const val SDK_CLIENT_HEADER = "Primer-SDK-Client"
        private const val CLIENT_TOKEN_HEADER = "Primer-Client-Token"
        private const val PRIMER_SDK_CHECKOUT_SESSION_ID_HEADER = "Primer-SDK-Checkout-Session-ID"
        private const val MAX_CACHE_SIZE_MB = 5 * 1024 * 1024L
        private const val CACHE_DIRECTORY = "primer_sdk_cache"
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 60L
    }
}
