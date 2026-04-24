package io.primer.statetransport.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ClientSessionInfoDataRequest(
    val locale: String,
    val platform: String,
    val returnUri: String,
) : JSONObjectSerializable {

    companion object Companion {
        private const val LOCALE_FIELD = "locale"
        private const val PLATFORM_FIELD = "platform"
        private const val REDIRECTION_URL_FIELD = "returnUri"

        @JvmField
        val serializer = JSONObjectSerializer<ClientSessionInfoDataRequest> { t ->
            JSONObject().apply {
                put(LOCALE_FIELD, t.locale)
                put(PLATFORM_FIELD, t.platform)
                put(REDIRECTION_URL_FIELD, t.returnUri)
            }
        }
    }
}
