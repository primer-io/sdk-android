package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import org.json.JSONObject

internal data class CreateSessionDataRequest(
    val merchantAccountId: String,
    val language: String,
    val currencyCode: String,
    val phoneNumber: String,
    val reference: String
) : JSONObjectSerializable {
    companion object {

        private const val MERCHANT_ACCOUNT_ID_FIELD = "merchant_account_id"
        private const val LANGUAGE_FIELD = "language"
        private const val CURRENCY_CODE_FIELD = "currency_code"
        private const val PHONE_NUMBER_FIELD = "phone_number"
        private const val REFERENCE_FIELD = "reference"

        @JvmField
        val serializer = object : JSONObjectSerializer<CreateSessionDataRequest> {
            override fun serialize(t: CreateSessionDataRequest): JSONObject {
                return JSONObject().apply {
                    put(MERCHANT_ACCOUNT_ID_FIELD, t.merchantAccountId)
                    put(LANGUAGE_FIELD, t.language)
                    put(CURRENCY_CODE_FIELD, t.currencyCode)
                    put(PHONE_NUMBER_FIELD, t.phoneNumber)
                    put(REFERENCE_FIELD, t.reference)
                }
            }
        }
    }
}

internal fun ApayaSessionParams.toCreateSessionRequest() =
    CreateSessionDataRequest(
        merchantAccountId,
        locale.language,
        currencyCode,
        phoneNumber,
        ""
    )
