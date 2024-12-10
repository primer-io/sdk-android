package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class NolPaySessionInfoDataRequest(
    private val mobileCountryCode: String,
    private val mobileNumber: String,
    private val nolPayCardNumber: String,
    val deviceVendor: String,
    val deviceModel: String,
    val platform: String = "ANDROID"
) : JSONObjectSerializable {
    companion object {

        private const val PLATFORM_FIELD = "platform"
        private const val MOBILE_COUNTRY_CODE_FIELD = "mobileCountryCode"
        private const val MOBILE_NUMBER_FIELD = "mobileNumber"
        private const val CARD_NUMBER_FIELD = "nolPayCardNumber"
        private const val PHONE_VENDOR_FIELD = "phoneVendor"
        private const val PHONE_MODEL_FIELD = "phoneModel"

        @JvmField
        val serializer = JSONObjectSerializer<NolPaySessionInfoDataRequest> { t ->
            JSONObject().apply {
                put(PLATFORM_FIELD, t.platform)
                put(MOBILE_COUNTRY_CODE_FIELD, t.mobileCountryCode)
                put(MOBILE_NUMBER_FIELD, t.mobileNumber)
                put(CARD_NUMBER_FIELD, t.nolPayCardNumber)
                put(PHONE_VENDOR_FIELD, t.deviceVendor)
                put(PHONE_MODEL_FIELD, t.deviceModel)
            }
        }
    }
}
