package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.core.di.DISdkComponent

internal class RemoteVaultedPaymentMethodDeleteDataSource(
    private val primerHttpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : DISdkComponent, BaseSuspendDataSource<Unit, BaseRemoteHostRequest<String>> {
    override suspend fun execute(input: BaseRemoteHostRequest<String>) =
        primerHttpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .delete<EmptyDataResponse>(
                url = "${input.host}/payment-instruments/${input.data}/vault",
                headers = apiVersion().toHeaderMap(),
            ).let { }
}
