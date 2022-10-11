package io.primer.android.data.rpc.retail_outlets.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.rpc.retail_outlets.models.RetailOutletDataRequest
import io.primer.android.data.rpc.retail_outlets.models.RetailOutletResultDataResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteRetailOutletFlowDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<RetailOutletResultDataResponse, BaseRemoteRequest<RetailOutletDataRequest>> {

    override fun execute(input: BaseRemoteRequest<RetailOutletDataRequest>) =
        primerHttpClient.post<RetailOutletDataRequest, RetailOutletResultDataResponse>(
            "${input.configuration.coreUrl}/xendit/checkout",
            input.data
        )
}
