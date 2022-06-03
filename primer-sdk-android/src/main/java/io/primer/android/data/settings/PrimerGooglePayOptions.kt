package io.primer.android.data.settings

import io.primer.android.payment.google.GooglePayButtonStyle
import kotlinx.serialization.Serializable

@Serializable
data class PrimerGooglePayOptions(
    var merchantName: String? = null,
    var allowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
    var buttonStyle: GooglePayButtonStyle = GooglePayButtonStyle.BLACK,
)
