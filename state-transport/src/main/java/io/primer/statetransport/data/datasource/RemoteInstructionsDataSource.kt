package io.primer.statetransport.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.data.network.retry.RetryConfig
import io.primer.statetransport.data.model.ClientSessionInstructionResponse

internal class RemoteInstructionsDataSource(
    private val httpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<
    PrimerResponse<ClientSessionInstructionResponse>,
    String,
    > {
    override suspend fun execute(input: String) =
        httpClient.retrySuspendGet<ClientSessionInstructionResponse>(
            url = input,
            headers = apiVersion().toHeaderMap(),
            retryConfig = RetryConfig(enabled = true),
        )
}
