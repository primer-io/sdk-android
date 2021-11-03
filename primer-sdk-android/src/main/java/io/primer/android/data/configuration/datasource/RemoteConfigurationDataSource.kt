package io.primer.android.data.configuration.datasource

import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) {

    fun getConfiguration(configurationUrl: String) = httpClient.get<Configuration>(
        configurationUrl
    )
}
