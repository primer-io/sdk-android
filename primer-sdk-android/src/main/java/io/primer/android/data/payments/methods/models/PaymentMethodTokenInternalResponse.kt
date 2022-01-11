package io.primer.android.data.payments.methods.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentMethodTokenInternalResponse(
    val data: List<PaymentMethodVaultTokenInternal>
)
