package io.primer.android.data.payments.async.datasource

import io.primer.android.data.payments.async.models.AsyncPaymentMethodStatusResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient
) {

    fun getAsyncStatus(statusUrl: String) = primerHttpClient.get<AsyncPaymentMethodStatusResponse>(
        statusUrl
    )
}
