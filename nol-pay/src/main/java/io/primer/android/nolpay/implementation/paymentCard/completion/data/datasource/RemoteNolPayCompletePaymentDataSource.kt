package io.primer.android.nolpay.implementation.paymentCard.completion.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT

internal class RemoteNolPayCompletePaymentDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<EmptyDataResponse, String> {
    override suspend fun execute(input: String) =
        httpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .suspendGet<EmptyDataResponse>(input).body
}
