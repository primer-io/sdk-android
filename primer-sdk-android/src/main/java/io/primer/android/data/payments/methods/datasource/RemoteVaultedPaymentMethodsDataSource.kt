package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.payments.methods.models.PaymentMethodTokenInternalResponse
import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration
import kotlinx.coroutines.flow.map

internal class RemoteVaultedPaymentMethodsDataSource(
    private val primerHttpClient: PrimerHttpClient,
) {

    fun getPaymentMethods(
        configuration: Configuration?,
    ) =
        primerHttpClient.get<PaymentMethodTokenInternalResponse>(
            "${configuration?.pciUrl}/payment-instruments",
        ).map { it.data }
}
