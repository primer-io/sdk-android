package io.primer.android.model.dto

import io.primer.android.model.PrimerDebugOptions
import io.primer.android.payment.google.GooglePayButtonStyle
import io.primer.android.utils.LocaleSerializer
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class Options(
    var preferWebView: Boolean = true,
    var klarnaWebViewTitle: String? = null,
    var apayaWebViewTitle: String? = null,
    var showUI: Boolean = true,
    var redirectScheme: String? = null,
    var is3DSOnVaultingEnabled: Boolean = false,
    var debugOptions: PrimerDebugOptions? = null,
    @Serializable(with = LocaleSerializer::class) var locale: Locale = Locale.getDefault(),
    var googlePayAllowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
    var googlePayButtonStyle: GooglePayButtonStyle =
        GooglePayButtonStyle.BLACK,
    var paymentHandling: PaymentHandling = PaymentHandling.AUTO
)
