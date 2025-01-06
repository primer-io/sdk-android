package io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.card

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData.VaultedPaymentMethodAdditionalDataValidator

internal class VaultedCardAdditionalDataValidator :
    VaultedPaymentMethodAdditionalDataValidator<PrimerVaultedCardAdditionalData> {
    override suspend fun validate(
        additionalData: PrimerVaultedCardAdditionalData,
        vaultedPaymentMethodData: PrimerVaultedPaymentMethod,
    ): List<PrimerValidationError> {
        return when {
            additionalData.cvv.length !=
                CardNetwork.lookupByCardNetwork(
                    vaultedPaymentMethodData.paymentInstrumentData.binData?.network.orEmpty(),
                ).cvvLength ->
                listOf(
                    PrimerValidationError(
                        INVALID_CVV_ERROR_ID,
                        "The length of the CVV does not match the required length.",
                    ),
                )
            DIGITS_ONLY_REGEX.matches(additionalData.cvv).not() ->
                listOf(
                    PrimerValidationError(
                        INVALID_CVV_ERROR_ID,
                        "Ensure that the CVV field consists exclusively of numerical digits.",
                    ),
                )
            else -> emptyList()
        }
    }

    companion object {
        private val DIGITS_ONLY_REGEX = "^\\d+\$".toRegex()
        const val INVALID_CVV_ERROR_ID = "invalid-cvv"
    }
}
