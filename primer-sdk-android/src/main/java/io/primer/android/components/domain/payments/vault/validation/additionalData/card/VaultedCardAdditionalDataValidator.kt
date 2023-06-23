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
                        "invalid-cvv", "CVV length does not match."
                    )
                )
            else -> emptyList()
        }
    }
}
