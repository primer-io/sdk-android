package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteKlarnaCustomerTokenDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<CreateCustomerTokenDataResponse,
        BaseRemoteRequest<CreateCustomerTokenDataRequest>> {
    override suspend fun execute(input: BaseRemoteRequest<CreateCustomerTokenDataRequest>):
        CreateCustomerTokenDataResponse = primerHttpClient.postSuspend(
        "${input.configuration.coreUrl}/klarna/customer-tokens",
        input.data
    )
}
