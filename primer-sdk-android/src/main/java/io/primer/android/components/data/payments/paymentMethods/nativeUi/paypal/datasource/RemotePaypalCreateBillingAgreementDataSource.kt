package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateBillingAgreementDataResponse
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class RemotePaypalCreateBillingAgreementDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<PaypalCreateBillingAgreementDataResponse,
        BaseRemoteRequest<PaypalCreateBillingAgreementDataRequest>> {

    override fun execute(input: BaseRemoteRequest<PaypalCreateBillingAgreementDataRequest>):
        Flow<PaypalCreateBillingAgreementDataResponse> {
        return primerHttpClient.post<PaypalCreateBillingAgreementDataRequest, PaypalCreateBillingAgreementDataResponse>(
            "${input.configuration.coreUrl}/paypal/billing-agreements/create-agreement",
            input.data
        ).mapLatest { responseBody -> responseBody.body }
    }
}
