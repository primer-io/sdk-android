package io.primer.statetransport.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ClientSessionMerchantDataRequest(
    val applicationId: String,
) : JSONObjectSerializable {

    companion object Companion {
        private const val APPLICATION_ID_FIELD = "applicationId"

        @JvmField
        val serializer = JSONObjectSerializer<ClientSessionMerchantDataRequest> { t ->
            JSONObject().apply {
                put(APPLICATION_ID_FIELD, t.applicationId)
            }
        }
    }
}
