package io.primer.android.payments.core.create.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderPair
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.payments.core.create.data.model.CreatePaymentDataRequest
import io.primer.android.payments.core.create.data.model.PaymentDataResponse

internal class CreatePaymentDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<PaymentDataResponse, BaseRemoteHostRequest<CreatePaymentDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<CreatePaymentDataRequest>): PaymentDataResponse {
        return primerHttpClient
            .withTimeout(PRIMER_15S_TIMEOUT)
            .suspendPost<CreatePaymentDataRequest, PaymentDataResponse>(
                url = "${input.host}/payments",
                request = input.data,
                headers =
                mapOf(
                    apiVersion().toHeaderPair(),
                    HEADER_ACCEPT to "*/*",
                ),
            ).body
    }

    private companion object {
        const val HEADER_ACCEPT = "accept"
    }
}
