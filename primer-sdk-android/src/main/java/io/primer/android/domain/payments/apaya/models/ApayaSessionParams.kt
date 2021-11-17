package io.primer.android.domain.payments.apaya.models

import io.primer.android.domain.base.Params
import java.util.Locale

internal data class ApayaSessionParams(
    val merchantAccountId: String,
    val locale: Locale,
    val currencyCode: String,
    val phoneNumber: String
) : Params
