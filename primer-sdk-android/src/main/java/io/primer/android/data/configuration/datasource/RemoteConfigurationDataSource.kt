package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseDataSource
import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) :
    BaseDataSource<Configuration, String> {
    override fun execute(input: String) = httpClient.get<Configuration>(
        input,
        mapOf(SDK_API_VERSION_HEADER to ApiVersion.CONFIGURATION_VERSION.version)
    )
}
