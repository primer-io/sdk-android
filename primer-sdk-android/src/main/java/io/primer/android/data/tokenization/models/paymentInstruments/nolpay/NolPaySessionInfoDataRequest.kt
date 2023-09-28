package io.primer.android.data.tokenization.models.paymentInstruments.nolpay

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal class NolPaySessionInfoDataRequest(
    private val mobileCountryCode: String,
    private val mobileNumber: String,
    private val nolPayCardNumber: String,
    val deviceVendor: String,
    val deviceModel: String,
    override val locale: String,
) : BaseSessionInfoDataRequest(locale) {
    companion object {

        private const val MOBILE_COUNTRY_CODE_FIELD = "mobileCountryCode"
        private const val MOBILE_NUMBER_FIELD = "mobileNumber"
        private const val CARD_NUMBER_FIELD = "nolPayCardNumber"
        private const val PHONE_VENDOR_FIELD = "phoneVendor"
        private const val PHONE_MODEL_FIELD = "phoneModel"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<NolPaySessionInfoDataRequest> {
                override fun serialize(t: NolPaySessionInfoDataRequest): JSONObject {
                    return JSONObject().apply {
                        put(PLATFORM_FIELD, t.platform)
                        put(LOCALE_FIELD, t.locale)
                        put(MOBILE_COUNTRY_CODE_FIELD, t.mobileCountryCode)
                        put(MOBILE_NUMBER_FIELD, t.mobileNumber)
                        put(CARD_NUMBER_FIELD, t.nolPayCardNumber)
                        put(PHONE_VENDOR_FIELD, t.deviceVendor)
                        put(PHONE_MODEL_FIELD, t.deviceModel)
                    }
                }
            }
    }
}
