package io.primer.android.data.settings

data class PrimerPaymentMethodOptions(
    var redirectScheme: String? = null,
    var googlePayOptions: PrimerGooglePayOptions = PrimerGooglePayOptions(),
    var klarnaOptions: PrimerKlarnaOptions = PrimerKlarnaOptions(),
    var apayaOptions: PrimerApayaOptions = PrimerApayaOptions(),
    var goCardlessOptions: PrimerGoCardlessOptions = PrimerGoCardlessOptions()
)
