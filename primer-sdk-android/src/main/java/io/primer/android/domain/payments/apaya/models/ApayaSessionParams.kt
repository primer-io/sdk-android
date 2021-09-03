package io.primer.android.domain.payments.apaya.models

import io.primer.android.data.payments.apaya.models.CreateSessionRequest
import java.util.Locale

internal data class ApayaSessionParams(
    val merchantId: String,
    val merchantAccountId: String,
    val locale: Locale,
    val currencyCode: String,
)

internal fun ApayaSessionParams.toCreateSessionRequest() =
    CreateSessionRequest(
        merchantId,
        merchantAccountId,
        locale.language,
        currencyCode,
        ""
    )
