package io.primer.android.data.payments.methods.datasource

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.data.payments.methods.models.BasePaymentMethodVaultExchangeDataRequest
import io.primer.android.http.PrimerHttpClient
import kotlin.reflect.KClass

internal class VaultedPaymentMethodExchangeDataSourceRegistry(httpClient: PrimerHttpClient) {

    private val registry: Map<KClass<out PrimerVaultedPaymentMethodAdditionalData>?,
        RemoteVaultedPaymentMethodsExchangeDataSource<BasePaymentMethodVaultExchangeDataRequest>> =
        mapOf(
            null to RemoteEmptyExchangeDataSource(httpClient),
            PrimerVaultedCardAdditionalData::class to RemoteVaultedCardExchangeDataSource(
                httpClient
            )
        )

    fun getExchangeDataSource(additionalData: PrimerVaultedPaymentMethodAdditionalData?) =
        registry[additionalData?.let { additionalData::class }] ?: throw IllegalArgumentException(
            EXCHANGE_DATA_SOURCE_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val EXCHANGE_DATA_SOURCE_NOT_SUPPORTED_MESSAGE = "Exchange datasource not registered."
    }
}
