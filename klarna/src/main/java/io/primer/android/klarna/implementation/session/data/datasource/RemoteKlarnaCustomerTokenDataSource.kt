package io.primer.android.klarna.implementation.session.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse

internal class RemoteKlarnaCustomerTokenDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<
        CreateCustomerTokenDataResponse,
        BaseRemoteHostRequest<CreateCustomerTokenDataRequest>,
        > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<CreateCustomerTokenDataRequest>,
    ): CreateCustomerTokenDataResponse =
        primerHttpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<CreateCustomerTokenDataRequest, CreateCustomerTokenDataResponse>(
                url = "${input.host}/klarna/customer-tokens",
                request = input.data,
            ).body
}
