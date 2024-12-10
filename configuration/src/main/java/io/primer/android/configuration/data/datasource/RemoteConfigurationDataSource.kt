package io.primer.android.configuration.data.datasource

import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.data.network.retry.RetryConfig
import io.primer.android.core.data.network.utils.Constants.SDK_API_VERSION_HEADER

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PrimerResponse<ConfigurationDataResponse>, String> {
    override suspend fun execute(input: String) = httpClient.retrySuspendGet<ConfigurationDataResponse>(
        url = input,
        headers = mapOf(SDK_API_VERSION_HEADER to CONFIGURATION_VERSION),
        retryConfig = RetryConfig(enabled = true)
    )

    private companion object {

        const val CONFIGURATION_VERSION = "2.2"
    }
}
