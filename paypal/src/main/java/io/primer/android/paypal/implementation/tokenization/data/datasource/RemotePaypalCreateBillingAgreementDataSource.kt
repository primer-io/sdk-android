package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataResponse

internal class RemotePaypalCreateBillingAgreementDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<
    PaypalCreateBillingAgreementDataResponse,
    BaseRemoteHostRequest<PaypalCreateBillingAgreementDataRequest>,
    > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<PaypalCreateBillingAgreementDataRequest>,
    ): PaypalCreateBillingAgreementDataResponse {
        return primerHttpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<PaypalCreateBillingAgreementDataRequest, PaypalCreateBillingAgreementDataResponse>(
                url = "${input.host}/paypal/billing-agreements/create-agreement",
                request = input.data,
            ).body
    }
}
