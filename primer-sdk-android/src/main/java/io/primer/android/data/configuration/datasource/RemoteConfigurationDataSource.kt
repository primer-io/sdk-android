package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) :
    BaseFlowDataSource<ConfigurationDataResponse, String> {
    override fun execute(input: String) = httpClient.get<ConfigurationDataResponse>(
        input,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.CONFIGURATION_VERSION.version)
    )
}
