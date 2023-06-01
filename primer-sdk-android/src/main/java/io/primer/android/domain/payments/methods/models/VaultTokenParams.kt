package io.primer.android.domain.payments.methods.models

import io.primer.android.domain.base.Params

internal data class VaultTokenParams(
    val vaultedPaymentMethodId: String,
    val paymentMethodType: String
) : Params
