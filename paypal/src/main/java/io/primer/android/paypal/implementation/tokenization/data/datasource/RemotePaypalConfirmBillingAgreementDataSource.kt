package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataResponse

internal class RemotePaypalConfirmBillingAgreementDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<
    PaypalConfirmBillingAgreementDataResponse,
    BaseRemoteHostRequest<PaypalConfirmBillingAgreementDataRequest>,
    > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<PaypalConfirmBillingAgreementDataRequest>,
    ): PaypalConfirmBillingAgreementDataResponse {
        return primerHttpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<PaypalConfirmBillingAgreementDataRequest, PaypalConfirmBillingAgreementDataResponse>(
                url = "${input.host}/paypal/billing-agreements/confirm-agreement",
                request = input.data,
            ).body
    }
}
