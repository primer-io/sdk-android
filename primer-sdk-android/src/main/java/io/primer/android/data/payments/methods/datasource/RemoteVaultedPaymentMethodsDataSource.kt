package io.primer.android.data.payments.methods.datasource

import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.payments.methods.models.PaymentMethodTokenInternalResponse
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient

internal class RemoteVaultedPaymentMethodsDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<List<PaymentMethodVaultTokenInternal>, ConfigurationData> {

    override suspend fun execute(input: ConfigurationData) =
        primerHttpClient.suspendGet<PaymentMethodTokenInternalResponse>(
            "${input.pciUrl}/payment-instruments",
            mapOf(SDK_API_VERSION_HEADER to ApiVersion.PAYMENT_INSTRUMENTS_VERSION.version)
        ).data
}
