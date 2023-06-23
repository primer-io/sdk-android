package io.primer.android.data.payments.methods.mapping.vault

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.data.payments.methods.mapping.vault.card.VaultedCardAdditionalDataMapper
import io.primer.android.data.payments.methods.mapping.vault.default.EmptyAdditionalDataMapper
import kotlin.reflect.KClass

internal class VaultedPaymentMethodAdditionalDataMapperRegistry {

    private val registry: Map<KClass<out PrimerVaultedPaymentMethodAdditionalData>?,
        VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedPaymentMethodAdditionalData?>> =
        mapOf(
            null to EmptyAdditionalDataMapper(),
            PrimerVaultedCardAdditionalData::class to VaultedCardAdditionalDataMapper()
        )

    fun getMapper(additionalData: PrimerVaultedPaymentMethodAdditionalData?):
        VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedPaymentMethodAdditionalData?> =
        registry[additionalData?.let { additionalData::class }] ?: throw IllegalArgumentException(
            ADDITIONAL_DATA_MAPPER_NOT_SUPPORTED_MESSAGE
        )

    private companion object {

        const val ADDITIONAL_DATA_MAPPER_NOT_SUPPORTED_MESSAGE =
            "Additional data mapper not registered."
    }
}
