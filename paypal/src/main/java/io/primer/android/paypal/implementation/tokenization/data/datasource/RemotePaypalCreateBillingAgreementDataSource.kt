package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient

internal class RemotePaypalCreateBillingAgreementDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<PaypalCreateBillingAgreementDataResponse,
        BaseRemoteHostRequest<PaypalCreateBillingAgreementDataRequest>> {

    override suspend fun execute(input: BaseRemoteHostRequest<PaypalCreateBillingAgreementDataRequest>):
        PaypalCreateBillingAgreementDataResponse {
        return primerHttpClient.suspendPost<
            PaypalCreateBillingAgreementDataRequest, PaypalCreateBillingAgreementDataResponse
            >(
            url = "${input.host}/paypal/billing-agreements/create-agreement",
            request = input.data
        ).body
    }
}
