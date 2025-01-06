package io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData

import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.card.VaultedCardAdditionalDataValidator
import kotlin.reflect.KClass

internal class VaultedPaymentMethodAdditionalDataValidatorRegistry {
    private val registry: Map<
        KClass<out PrimerVaultedPaymentMethodAdditionalData>,
        VaultedPaymentMethodAdditionalDataValidator<PrimerVaultedPaymentMethodAdditionalData>,
        > =
        mapOf(PrimerVaultedCardAdditionalData::class to VaultedCardAdditionalDataValidator())

    fun getValidator(
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ): VaultedPaymentMethodAdditionalDataValidator<PrimerVaultedPaymentMethodAdditionalData> =
        registry[additionalData::class] ?: throw IllegalArgumentException(
            ADDITIONAL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE,
        )

    private companion object {
        const val ADDITIONAL_DATA_VALIDATOR_NOT_SUPPORTED_MESSAGE =
            "Vaulted payment method additional data validator not registered"
    }
}
