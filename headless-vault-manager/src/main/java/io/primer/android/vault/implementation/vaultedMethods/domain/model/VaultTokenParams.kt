package io.primer.android.vault.implementation.vaultedMethods.domain.model

import io.primer.android.core.domain.Params
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

internal data class VaultTokenParams(
    val vaultedPaymentMethodId: String,
    val paymentMethodType: String,
    val additionalData: PrimerVaultedPaymentMethodAdditionalData? = null
) : Params
