package io.primer.android.stripe.ach.implementation.payment.confirmation.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.model.StripeAchCompletePaymentDataRequest

internal class RemoteStripeAchCompletePaymentDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<EmptyDataResponse, BaseRemoteUrlRequest<StripeAchCompletePaymentDataRequest>> {
    override suspend fun execute(input: BaseRemoteUrlRequest<StripeAchCompletePaymentDataRequest>): EmptyDataResponse {
        return primerHttpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<StripeAchCompletePaymentDataRequest, EmptyDataResponse>(
                url = input.url,
                request = input.data,
            ).body
    }
}
