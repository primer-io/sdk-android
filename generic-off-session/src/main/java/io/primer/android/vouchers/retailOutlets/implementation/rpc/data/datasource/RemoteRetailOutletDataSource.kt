package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletResultDataResponse

internal class RemoteRetailOutletDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<RetailOutletResultDataResponse, BaseRemoteHostRequest<RetailOutletDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<RetailOutletDataRequest>) =
        primerHttpClient
            .withTimeout(PRIMER_60S_TIMEOUT)
            .suspendGet<RetailOutletResultDataResponse>(
                url =
                    "${input.host}/payment-method-options/" +
                        "${input.data.paymentMethodConfigId}/retail-outlets",
            ).body
}
