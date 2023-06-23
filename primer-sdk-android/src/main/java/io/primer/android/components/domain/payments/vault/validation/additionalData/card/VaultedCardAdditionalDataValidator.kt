package io.primer.android.components.domain.payments.vault.validation.additionalData.card

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.components.domain.payments.vault.validation.additionalData.VaultedPaymentMethodAdditionalDataValidator
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.ui.CardNetwork

internal class VaultedCardAdditionalDataValidator :
    VaultedPaymentMethodAdditionalDataValidator<PrimerVaultedCardAdditionalData> {
    override suspend fun validate(
        additionalData: PrimerVaultedCardAdditionalData,
        vaultedPaymentMethodData: PrimerVaultedPaymentMethod
    ): List<PrimerValidationError> {
        return when {
            additionalData.cvv.length !=
                CardNetwork.lookupByCardNetwork(
                    vaultedPaymentMethodData.paymentInstrumentData.binData?.network.orEmpty()
                ).cvvLength ->
                listOf(
                    PrimerValidationError(
                        INVALID_CVV_ERROR_ID,
                        "The length of the CVV does not match the required length."
                    )
                )
            DIGITS_ONLY_REGEX.matches(additionalData.cvv).not() ->
                listOf(
                    PrimerValidationError(
                        INVALID_CVV_ERROR_ID,
                        "Ensure that the CVV field consists exclusively of numerical digits."
                    )
                )
            else -> emptyList()
        }
    }

    companion object {

        const val INVALID_CVV_ERROR_ID = "invalid-cvv"
        val DIGITS_ONLY_REGEX = "^\\d+\$".toRegex()
    }
}
