package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.payments.methods.models.PaymentMethodTokenInternalResponse
import io.primer.android.http.PrimerHttpClient
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import kotlinx.coroutines.flow.map

internal class RemoteVaultedPaymentMethodsDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<List<PaymentMethodVaultTokenInternal>, Configuration> {

    override fun execute(input: Configuration) =
        primerHttpClient.get<PaymentMethodTokenInternalResponse>(
            "${input.pciUrl}/payment-instruments",
            mapOf(SDK_API_VERSION_HEADER to ApiVersion.PAYMENT_INSTRUMENTS_VERSION.version)
        ).map { it.data }
}
