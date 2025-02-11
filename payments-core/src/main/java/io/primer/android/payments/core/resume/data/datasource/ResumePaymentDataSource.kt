package io.primer.android.payments.core.resume.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import io.primer.android.payments.core.resume.data.model.ResumePaymentDataRequest

internal class ResumePaymentDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<
    PaymentDataResponse,
    BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>,
    > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>,
    ): PaymentDataResponse {
        return primerHttpClient
            .withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<ResumePaymentDataRequest, PaymentDataResponse>(
                url = "${input.host}/payments/${input.data.first}/resume",
                request = input.data.second,
                headers = apiVersion().toHeaderMap(),
            ).body
    }
}
