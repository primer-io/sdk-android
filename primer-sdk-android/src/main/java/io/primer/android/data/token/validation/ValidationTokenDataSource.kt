package io.primer.android.data.token.validation

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.token.validation.model.ValidationTokenRequestData
import io.primer.android.domain.token.model.TokenCheckStatusResponse
import io.primer.android.http.PrimerHttpClient

internal class ValidationTokenDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<TokenCheckStatusResponse, BaseRemoteRequest<ValidationTokenRequestData>> {

    override fun execute(input: BaseRemoteRequest<ValidationTokenRequestData>) =
        primerHttpClient.post<ValidationTokenRequestData, TokenCheckStatusResponse>(
            "${input.configuration.pciUrl}/client-token/validate",
            input.data
        )
}
