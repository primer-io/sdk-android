package io.primer.android.klarna.implementation.session.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse

internal class RemoteKlarnaCustomerTokenDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<CreateCustomerTokenDataResponse,
        BaseRemoteHostRequest<CreateCustomerTokenDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<CreateCustomerTokenDataRequest>):
        CreateCustomerTokenDataResponse = primerHttpClient.suspendPost<
        CreateCustomerTokenDataRequest, CreateCustomerTokenDataResponse
        >(
        "${input.host}/klarna/customer-tokens",
        input.data
    ).body
}
