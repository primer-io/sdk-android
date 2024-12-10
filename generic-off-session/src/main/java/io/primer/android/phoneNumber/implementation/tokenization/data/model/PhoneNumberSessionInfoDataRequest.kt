package io.primer.android.phoneNumber.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class PhoneNumberSessionInfoDataRequest(
    val locale: String,
    val phoneNumber: String,
    val platform: String = "ANDROID"
) : JSONObjectSerializable {

    companion object {

        private const val PLATFORM_FIELD = "platform"
        private const val PHONE_NUMBER_FIELD = "phoneNumber"
        private const val LOCALE_FIELD = "locale"

        @JvmField
        val serializer = JSONObjectSerializer<PhoneNumberSessionInfoDataRequest> { t ->
            JSONObject().apply {
                put(PLATFORM_FIELD, t.platform)
                put(LOCALE_FIELD, t.locale)
                put(PHONE_NUMBER_FIELD, t.phoneNumber)
            }
        }
    }
}
