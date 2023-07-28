package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalConfirmBillingAgreementDataResponse
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow

internal class RemotePaypalConfirmBillingAgreementDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<PaypalConfirmBillingAgreementDataResponse,
        BaseRemoteRequest<PaypalConfirmBillingAgreementDataRequest>> {

    override fun execute(input: BaseRemoteRequest<PaypalConfirmBillingAgreementDataRequest>):
        Flow<PaypalConfirmBillingAgreementDataResponse> {
        return primerHttpClient.post(
            "${input.configuration.coreUrl}/paypal/billing-agreements/confirm-agreement",
            input.data
        )
    }
}
