package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.Constants.SDK_API_VERSION_HEADER
import io.primer.android.vault.implementation.utils.Constants

internal class RemoteVaultedPaymentMethodDeleteDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<Unit, BaseRemoteHostRequest<String>> {
    override suspend fun execute(input: BaseRemoteHostRequest<String>) =
        primerHttpClient.delete<EmptyDataResponse>(
            "${input.host}/payment-instruments/${input.data}/vault",
            mapOf(SDK_API_VERSION_HEADER to Constants.PAYMENT_INSTRUMENTS_VERSION),
        ).let { }
}
