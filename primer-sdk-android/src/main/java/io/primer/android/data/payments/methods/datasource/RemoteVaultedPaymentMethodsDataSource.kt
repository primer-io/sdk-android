package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.base.datasource.BaseDataSource
import io.primer.android.data.payments.methods.models.PaymentMethodTokenInternalResponse
import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.coroutines.flow.map

internal class RemoteVaultedPaymentMethodsDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseDataSource<List<PaymentMethodTokenInternal>, Configuration>() {

    override fun execute(input: Configuration) =
        primerHttpClient.get<PaymentMethodTokenInternalResponse>(
            "${input.pciUrl}/payment-instruments",
        ).map { it.data }
}
