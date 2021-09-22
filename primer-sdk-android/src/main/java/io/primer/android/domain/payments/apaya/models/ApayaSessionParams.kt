package io.primer.android.domain.payments.apaya.models

import io.primer.android.data.payments.apaya.models.CreateSessionRequest
import java.util.Locale

internal data class ApayaSessionParams(
    val merchantAccountId: String,
    val locale: Locale,
    val currencyCode: String,
    val phoneNumber: String
)

internal fun ApayaSessionParams.toCreateSessionRequest() =
    CreateSessionRequest(
        merchantAccountId,
        locale.language,
        currencyCode,
        phoneNumber,
        ""
    )
