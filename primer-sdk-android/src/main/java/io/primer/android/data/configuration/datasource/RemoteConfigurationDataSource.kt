package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.di.ApiVersion
import io.primer.android.di.NetworkContainer.Companion.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import io.primer.android.http.retry.RetryConfig

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) :
    BaseFlowDataSource<ConfigurationDataResponse, String> {
    override fun execute(input: String) = httpClient.retryGet<ConfigurationDataResponse>(
        input,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.CONFIGURATION_VERSION.version),
        RetryConfig(true)
    )
}
