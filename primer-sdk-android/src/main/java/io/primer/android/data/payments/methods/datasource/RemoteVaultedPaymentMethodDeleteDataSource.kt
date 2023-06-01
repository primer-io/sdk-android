package io.primer.android.data.payments.methods.datasource

import io.primer.android.core.data.models.EmptyDataRequest
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient

internal class RemoteVaultedPaymentMethodDeleteDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<Unit, BaseRemoteRequest<String>> {

    override suspend fun execute(input: BaseRemoteRequest<String>) =
        primerHttpClient.delete<EmptyDataRequest>(
            "${input.configuration.pciUrl}/payment-instruments/${input.data}/vault",
            mapOf(SDK_API_VERSION_HEADER to ApiVersion.PAYMENT_INSTRUMENTS_VERSION.version)
        ).let { }
}
