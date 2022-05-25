package io.primer.android.data.settings

import kotlinx.serialization.Serializable

@Serializable
data class PrimerCardPaymentOptions(
    var is3DSOnVaultingEnabled: Boolean = true
)
