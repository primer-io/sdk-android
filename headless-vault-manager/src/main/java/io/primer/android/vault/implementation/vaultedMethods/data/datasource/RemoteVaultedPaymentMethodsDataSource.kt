package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodTokenInternalResponse
import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodVaultTokenInternal

internal class RemoteVaultedPaymentMethodsDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<List<PaymentMethodVaultTokenInternal>, ConfigurationData> {
    override suspend fun execute(input: ConfigurationData) =
        primerHttpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .suspendGet<PaymentMethodTokenInternalResponse>(
                url = "${input.pciUrl}/payment-instruments",
                headers = apiVersion().toHeaderMap(),
            ).body.data
}
