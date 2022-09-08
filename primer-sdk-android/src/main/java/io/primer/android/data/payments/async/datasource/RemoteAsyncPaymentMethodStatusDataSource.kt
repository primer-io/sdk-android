package io.primer.android.data.payments.async.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.payments.async.models.AsyncPaymentMethodStatusDataResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<AsyncPaymentMethodStatusDataResponse, String> {

    override fun execute(input: String) =
        primerHttpClient.get<AsyncPaymentMethodStatusDataResponse>(
            input
        )
}
