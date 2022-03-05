package io.primer.android.components.ui.assets

enum class Brand(internal val brandName: String) {
    PAYPAL("paypal"),
    GOOGLE_PAY("googlepay"),
    KLARNA("klarna"),
    APAYA("apaya"),
    PAYMENT_CARD("credit_card")
}

enum class ImageType {
    LOGO, ICON
}

enum class ImageColor {
    ORIGINAL, LIGHT, DARK
}
