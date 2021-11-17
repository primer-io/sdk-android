package io.primer.android.data.payments.apaya.models

import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSessionRequest(
    @SerialName("merchant_account_id") val merchantAccountId: String,
    val language: String,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("phone_number") val phoneNumber: String,
    val reference: String,
)

internal fun ApayaSessionParams.toCreateSessionRequest() =
    CreateSessionRequest(
        merchantAccountId,
        locale.language,
        currencyCode,
        phoneNumber,
        ""
    )
