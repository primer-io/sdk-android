package io.primer.android.data.payments.apaya.models

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
