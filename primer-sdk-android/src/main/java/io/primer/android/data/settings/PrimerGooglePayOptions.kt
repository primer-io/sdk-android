package io.primer.android.data.settings

enum class GooglePayButtonStyle {
    WHITE,
    BLACK,
}

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
    var captureBillingAddress: Boolean = false
)
