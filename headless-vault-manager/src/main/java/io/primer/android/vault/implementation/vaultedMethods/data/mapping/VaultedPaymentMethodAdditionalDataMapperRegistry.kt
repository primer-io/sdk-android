package io.primer.android.vault.implementation.vaultedMethods.data.mapping

import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.card.VaultedCardAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.default.EmptyAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlin.reflect.KClass

internal class VaultedPaymentMethodAdditionalDataMapperRegistry {
    private val registry: Map<
        KClass<out PrimerVaultedPaymentMethodAdditionalData>?,
        VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedPaymentMethodAdditionalData?>,
        > =
        mapOf(
            null to EmptyAdditionalDataMapper(),
            PrimerVaultedCardAdditionalData::class to VaultedCardAdditionalDataMapper(),
        )

    fun getMapper(
        additionalData: PrimerVaultedPaymentMethodAdditionalData?,
    ): VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedPaymentMethodAdditionalData?> =
        registry[additionalData?.let { additionalData::class }] ?: throw IllegalArgumentException(
            ADDITIONAL_DATA_MAPPER_NOT_SUPPORTED_MESSAGE,
        )

    private companion object {
        const val ADDITIONAL_DATA_MAPPER_NOT_SUPPORTED_MESSAGE =
            "Additional data mapper not registered."
    }
}
