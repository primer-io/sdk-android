package io.primer.android.nolpay.implementation.paymentCard.completion.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient

internal class RemoteNolPayCompletePaymentDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<EmptyDataResponse, String> {
    override suspend fun execute(input: String) = httpClient.suspendGet<EmptyDataResponse>(input).body
}
