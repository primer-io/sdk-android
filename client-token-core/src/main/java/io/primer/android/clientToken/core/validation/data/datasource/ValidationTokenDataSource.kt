package io.primer.android.clientToken.core.validation.data.datasource

import io.primer.android.clientToken.core.validation.data.model.TokenCheckStatusDataResponse
import io.primer.android.clientToken.core.validation.data.model.ValidationTokenDataRequest
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient

internal class ValidationTokenDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<
        TokenCheckStatusDataResponse,
        BaseRemoteHostRequest<ValidationTokenDataRequest>,
        > {
    override suspend fun execute(input: BaseRemoteHostRequest<ValidationTokenDataRequest>) =
        primerHttpClient.suspendPost<ValidationTokenDataRequest, TokenCheckStatusDataResponse>(
            url = "${input.host}/client-token/validate",
            request = input.data,
        ).body
}
