package io.primer.android.data.tokenization.models.paymentInstruments.async.phone

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class PhoneNumberSessionInfoDataRequest(
    val phoneNumber: String,
    override val locale: String,
    override val redirectionUrl: String,
    override val platform: String = "ANDROID"
) : BaseSessionInfoDataRequest(locale, redirectionUrl, platform) {
    companion object {

        private const val PHONE_NUMBER_FIELD = "phoneNumber"

        @JvmField
        val serializer = object : JSONObjectSerializer<PhoneNumberSessionInfoDataRequest> {
            override fun serialize(t: PhoneNumberSessionInfoDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                    put(REDIRECTION_URL_FIELD, t.redirectionUrl)
                    put(PHONE_NUMBER_FIELD, t.phoneNumber)
                }
            }
        }
    }
}
