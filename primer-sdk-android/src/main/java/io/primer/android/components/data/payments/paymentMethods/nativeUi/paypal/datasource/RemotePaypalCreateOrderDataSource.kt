package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderDataResponse
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow

internal class RemotePaypalCreateOrderDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<PaypalCreateOrderDataResponse,
        BaseRemoteRequest<PaypalCreateOrderDataRequest>> {

    override fun execute(input: BaseRemoteRequest<PaypalCreateOrderDataRequest>):
        Flow<PaypalCreateOrderDataResponse> {
        return primerHttpClient.post(
            "${input.configuration.coreUrl}/paypal/orders/create",
            input.data
        )
    }
}
