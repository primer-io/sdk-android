package io.primer.android.clientToken.core.validation.data.datasource

import io.primer.android.clientToken.core.validation.data.model.TokenCheckStatusDataResponse
import io.primer.android.clientToken.core.validation.data.model.ValidationTokenDataRequest
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import kotlin.time.Duration

internal class ValidationTokenDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<TokenCheckStatusDataResponse, BaseRemoteHostRequest<ValidationTokenDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<ValidationTokenDataRequest>) =
        primerHttpClient
            .withTimeout(PRIMER_15S_TIMEOUT)
            .suspendPost<ValidationTokenDataRequest, TokenCheckStatusDataResponse>(
                url = "${input.host}/client-token/validate",
                request = input.data,
                headers = apiVersion().toHeaderMap(),
            ).body

    data class RequestSettings(
        val timeout: Duration,
        val headers: Map<String, String>,
    )
}
