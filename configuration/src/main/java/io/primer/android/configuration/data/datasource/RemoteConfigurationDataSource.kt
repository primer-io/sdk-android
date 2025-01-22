package io.primer.android.configuration.data.datasource

import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.data.network.retry.RetryConfig
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT

internal class RemoteConfigurationDataSource(
    private val httpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<PrimerResponse<ConfigurationDataResponse>, String> {
    override suspend fun execute(input: String) =
        httpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .retrySuspendGet<ConfigurationDataResponse>(
                url = input,
                headers = apiVersion().toHeaderMap(),
                retryConfig = RetryConfig(enabled = true),
            )
}
