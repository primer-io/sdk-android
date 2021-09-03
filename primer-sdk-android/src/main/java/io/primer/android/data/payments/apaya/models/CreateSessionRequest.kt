package io.primer.android.data.payments.apaya.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSessionRequest(
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("merchant_account_id") val merchantAccountId: String,
    val language: String,
    @SerialName("currency_code") val currencyCode: String,
    val reference: String,
)
