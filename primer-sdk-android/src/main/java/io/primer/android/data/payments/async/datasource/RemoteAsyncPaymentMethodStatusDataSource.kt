package io.primer.android.data.payments.async.datasource

import io.primer.android.data.base.datasource.BaseDataSource
import io.primer.android.data.payments.async.models.AsyncPaymentMethodStatusResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseDataSource<AsyncPaymentMethodStatusResponse, String> {

    override fun execute(input: String) = primerHttpClient.get<AsyncPaymentMethodStatusResponse>(
        input
    )
}
