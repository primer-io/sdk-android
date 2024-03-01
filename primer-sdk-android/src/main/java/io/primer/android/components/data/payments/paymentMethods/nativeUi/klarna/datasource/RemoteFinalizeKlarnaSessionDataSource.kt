package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteFinalizeKlarnaSessionDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<FinalizeKlarnaSessionDataResponse,
        BaseRemoteRequest<FinalizeKlarnaSessionDataRequest>> {
    override suspend fun execute(
        input: BaseRemoteRequest<FinalizeKlarnaSessionDataRequest>
    ): FinalizeKlarnaSessionDataResponse =
        primerHttpClient.postSuspend(
            "${input.configuration.coreUrl}/klarna/payment-sessions/finalize",
            input.data
        )
}
