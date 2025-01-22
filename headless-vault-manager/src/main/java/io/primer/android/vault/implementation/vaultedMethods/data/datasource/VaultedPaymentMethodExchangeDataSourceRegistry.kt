package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.vault.implementation.vaultedMethods.data.model.BasePaymentMethodVaultExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlin.reflect.KClass

internal class VaultedPaymentMethodExchangeDataSourceRegistry(
    primerHttpClient: PrimerHttpClient,
    apiVersion: () -> PrimerApiVersion,
) {
    private val registry: Map<
        KClass<out PrimerVaultedPaymentMethodAdditionalData>?,
        RemoteVaultedPaymentMethodsExchangeDataSource<BasePaymentMethodVaultExchangeDataRequest>,
        > =
        mapOf(
            null to RemoteEmptyExchangeDataSource(primerHttpClient, apiVersion),
            PrimerVaultedCardAdditionalData::class to RemoteVaultedCardExchangeDataSource(primerHttpClient, apiVersion),
        )

    fun getExchangeDataSource(additionalData: PrimerVaultedPaymentMethodAdditionalData?) =
        registry[additionalData?.let { additionalData::class }] ?: throw IllegalArgumentException(
            EXCHANGE_DATA_SOURCE_NOT_SUPPORTED_MESSAGE,
        )

    private companion object {
        const val EXCHANGE_DATA_SOURCE_NOT_SUPPORTED_MESSAGE = "Exchange datasource not registered."
    }
}
