package io.primer.android.data.rpc.retailOutlets.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.rpc.retailOutlets.models.RetailOutletDataRequest
import io.primer.android.data.rpc.retailOutlets.models.RetailOutletResultDataResponse
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.mapLatest

internal class RemoteRetailOutletFlowDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<RetailOutletResultDataResponse, BaseRemoteRequest<RetailOutletDataRequest>> {

    override fun execute(input: BaseRemoteRequest<RetailOutletDataRequest>) =
        primerHttpClient.get<RetailOutletResultDataResponse>(
            "${input.configuration.coreUrl}/payment-method-options/" +
                "${input.data.paymentMethodConfigId}/retail-outlets"
        ).mapLatest { responseBody -> responseBody.body }
}
