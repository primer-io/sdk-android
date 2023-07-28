package io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models

import java.util.Locale

internal data class ApayaSessionConfiguration(
    val merchantAccountId: String,
    val locale: Locale,
    val currencyCode: String,
    val phoneNumber: String
)
