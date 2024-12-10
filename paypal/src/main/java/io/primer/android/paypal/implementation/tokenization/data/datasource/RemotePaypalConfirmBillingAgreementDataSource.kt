package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient

internal class RemotePaypalConfirmBillingAgreementDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<PaypalConfirmBillingAgreementDataResponse,
        BaseRemoteHostRequest<PaypalConfirmBillingAgreementDataRequest>> {

    override suspend fun execute(input: BaseRemoteHostRequest<PaypalConfirmBillingAgreementDataRequest>):
        PaypalConfirmBillingAgreementDataResponse {
        return primerHttpClient.suspendPost<
            PaypalConfirmBillingAgreementDataRequest, PaypalConfirmBillingAgreementDataResponse
            >(
            url = "${input.host}/paypal/billing-agreements/confirm-agreement",
            request = input.data
        ).body
    }
}
