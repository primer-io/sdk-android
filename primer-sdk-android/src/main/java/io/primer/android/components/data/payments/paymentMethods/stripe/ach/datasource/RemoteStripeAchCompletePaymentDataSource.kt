package io.primer.android.components.data.payments.paymentMethods.stripe.ach.datasource

import io.primer.android.components.data.payments.paymentMethods.stripe.ach.model.StripeAchCompletePaymentDataRequest
import io.primer.android.core.data.models.EmptyDataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteStripeAchCompletePaymentDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<
        EmptyDataResponse,
        BaseRemoteUrlRequest<StripeAchCompletePaymentDataRequest>
        > {
    override suspend fun execute(
        input: BaseRemoteUrlRequest<StripeAchCompletePaymentDataRequest>
    ): EmptyDataResponse {
        return primerHttpClient.postSuspend<StripeAchCompletePaymentDataRequest, EmptyDataResponse>(
            url = input.url,
            request = input.data
        )
    }
}
