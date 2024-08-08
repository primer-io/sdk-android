package io.primer.android.data.payments.status.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.payments.status.models.AsyncPaymentMethodStatusDataResponse
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.mapLatest

internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<AsyncPaymentMethodStatusDataResponse, String> {

    override fun execute(input: String) =
        primerHttpClient.get<AsyncPaymentMethodStatusDataResponse>(
            input
        ).mapLatest { responseData -> responseData.body }
}
