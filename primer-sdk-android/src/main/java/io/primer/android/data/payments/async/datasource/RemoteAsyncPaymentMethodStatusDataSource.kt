package io.primer.android.data.payments.async.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.payments.async.models.AsyncPaymentMethodStatusResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<AsyncPaymentMethodStatusResponse, String> {

    override fun execute(input: String) = primerHttpClient.get<AsyncPaymentMethodStatusResponse>(
        input
    )
}
