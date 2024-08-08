package io.primer.android.components.data.payments.paymentMethods.nolpay.datasource

import io.primer.android.core.data.models.EmptyDataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.http.PrimerHttpClient

internal class RemoteNolPayCompletePaymentDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<EmptyDataResponse, String> {
    override suspend fun execute(input: String) = httpClient.suspendGet<EmptyDataResponse>(input).body
}
