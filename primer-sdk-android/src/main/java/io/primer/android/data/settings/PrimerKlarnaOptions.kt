package io.primer.android.data.settings

import kotlinx.serialization.Serializable

@Serializable
data class PrimerKlarnaOptions(
    var recurringPaymentDescription: String? = null,
    @Deprecated("This property is deprecated and will be removed in future release.")
    var webViewTitle: String? = null,
)
