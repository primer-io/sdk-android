package io.primer.android.otp.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal abstract class OtpSessionInfoDataRequest(
    val platform: String = "ANDROID"
) : JSONObjectSerializable {
    abstract val locale: String

    abstract fun serialize(): JSONObject

    companion object {

        private const val PLATFORM_FIELD = "platform"
        private const val LOCALE_FIELD = "locale"

        val serializer = JSONObjectSerializer<OtpSessionInfoDataRequest> { t ->
            JSONObject().apply {
                put(PLATFORM_FIELD, t.platform)
                put(LOCALE_FIELD, t.locale)
            }
        }
    }
}

internal data class AdyenBlikSessionInfoDataRequest(
    override val locale: String,
    val blikCode: String
) : OtpSessionInfoDataRequest(locale) {

    override fun serialize(): JSONObject = OtpSessionInfoDataRequest.serializer.serialize(this).apply {
        put(BLIK_CODE_FIELD, blikCode)
    }

    companion object {
        private const val BLIK_CODE_FIELD = "blikCode"
    }
}
