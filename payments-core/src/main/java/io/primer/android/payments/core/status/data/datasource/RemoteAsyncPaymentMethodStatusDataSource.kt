package io.primer.android.payments.core.status.data.datasource

import io.primer.android.core.data.datasource.BaseFlowDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.payments.core.status.data.models.AsyncPaymentMethodStatusDataResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
internal class RemoteAsyncPaymentMethodStatusDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<AsyncPaymentMethodStatusDataResponse, String> {
    override fun execute(input: String) =
        primerHttpClient.get<AsyncPaymentMethodStatusDataResponse>(url = input)
            .mapLatest { response ->
                response.body
            }
}
