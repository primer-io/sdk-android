package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseDataSource
import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration

internal class RemoteConfigurationDataSource(private val httpClient: PrimerHttpClient) :
    BaseDataSource<Configuration, String>() {
    override fun execute(input: String) = httpClient.get<Configuration>(
        input
    )
}
