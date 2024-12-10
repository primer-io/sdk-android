package io.primer.android.stripe.ach.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class StripeAchSessionInfoDataRequest(
    val locale: String,
    val platform: String = "ANDROID"
) : JSONObjectSerializable {
    companion object {

        private const val PLATFORM_FIELD = "platform"
        private const val LOCALE_FIELD = "locale"

        @JvmField
        val serializer =
            JSONObjectSerializer<StripeAchSessionInfoDataRequest> { t ->
                JSONObject().apply {
                    put(PLATFORM_FIELD, t.platform)
                    put(LOCALE_FIELD, t.locale)
                }
            }
    }
}
