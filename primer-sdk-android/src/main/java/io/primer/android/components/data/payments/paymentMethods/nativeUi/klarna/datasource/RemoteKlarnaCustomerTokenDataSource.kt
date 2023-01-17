package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow

internal class RemoteKlarnaCustomerTokenDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<CreateCustomerTokenDataResponse,
        BaseRemoteRequest<CreateCustomerTokenDataRequest>> {
    override fun execute(input: BaseRemoteRequest<CreateCustomerTokenDataRequest>):
        Flow<CreateCustomerTokenDataResponse> {
        return primerHttpClient.post(
            "${input.configuration.coreUrl}/klarna/customer-tokens",
            input.data
        )
    }
}
