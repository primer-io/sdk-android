package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.http.PrimerHttpClient

internal class RemoteVaultedPaymentMethodsExchangeDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<PaymentMethodTokenInternal, BaseRemoteRequest<String>> {

    override fun execute(input: BaseRemoteRequest<String>) =
        primerHttpClient.post<Unit, PaymentMethodTokenInternal>(
            "${input.configuration.pciUrl}/payment-instruments/${input.data}/exchange",
            Unit
        )
}
