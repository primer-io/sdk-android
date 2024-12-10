package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.Constants.SDK_API_VERSION_HEADER
import io.primer.android.vault.implementation.utils.Constants
import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodTokenInternalResponse
import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodVaultTokenInternal

internal class RemoteVaultedPaymentMethodsDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<List<PaymentMethodVaultTokenInternal>, ConfigurationData> {

    override suspend fun execute(input: ConfigurationData) =
        primerHttpClient.suspendGet<PaymentMethodTokenInternalResponse>(
            "${input.pciUrl}/payment-instruments",
            mapOf(SDK_API_VERSION_HEADER to Constants.PAYMENT_INSTRUMENTS_VERSION)
        ).body.data
}
