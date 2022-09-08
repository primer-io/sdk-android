package io.primer.android.data.token.validation

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.token.validation.model.ValidationTokenDataRequest
import io.primer.android.domain.token.model.TokenCheckStatusDataResponse
import io.primer.android.http.PrimerHttpClient

internal class ValidationTokenDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<
        TokenCheckStatusDataResponse, BaseRemoteRequest<ValidationTokenDataRequest>
        > {

    override fun execute(input: BaseRemoteRequest<ValidationTokenDataRequest>) =
        primerHttpClient.post<ValidationTokenDataRequest, TokenCheckStatusDataResponse>(
            "${input.configuration.pciUrl}/client-token/validate",
            input.data
        )
}
