// package structure is kept in order to maintain backward compatibility
package io.primer.android.components.domain.payments.vault.model.card

import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

data class PrimerVaultedCardAdditionalData(val cvv: String) : PrimerVaultedPaymentMethodAdditionalData
