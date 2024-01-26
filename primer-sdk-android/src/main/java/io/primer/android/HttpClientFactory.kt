package io.primer.android

import io.primer.android.core.logging.BlacklistedHttpHeaderProviderRegistry
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import okhttp3.Interceptor
import okhttp3.OkHttpClient

internal class HttpClientFactory(
    private val logReporter: LogReporter,
    private val blacklistedHttpHeaderProviderRegistry: BlacklistedHttpHeaderProviderRegistry,
    private val whitelistedHttpBodyKeyProviderRegistry: WhitelistedHttpBodyKeyProviderRegistry,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) {
    fun build(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                    .let { chain.proceed(it) }
            }
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggerInterceptor(
                    logReporter = logReporter,
                    blacklistedHttpHeaderProviderRegistry = blacklistedHttpHeaderProviderRegistry,
                    whitelistedHttpBodyKeyProviderRegistry = whitelistedHttpBodyKeyProviderRegistry,
                    localConfigurationDataSource = localConfigurationDataSource
                )
            )
        }
        return builder.build()
    }
}
