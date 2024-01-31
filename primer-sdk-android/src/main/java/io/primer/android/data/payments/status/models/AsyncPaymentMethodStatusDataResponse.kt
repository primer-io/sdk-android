package io.primer.android.data.payments.status.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import org.json.JSONObject

internal data class AsyncPaymentMethodStatusDataResponse(
    val id: String,
    val status: AsyncMethodStatus,
    val source: String
) : JSONDeserializable {
    companion object {
        private const val ID_FIELD = "id"
        private const val STATUS_FIELD = "status"
        private const val SOURCE_FIELD = "source"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<AsyncPaymentMethodStatusDataResponse> {

            override fun deserialize(t: JSONObject): AsyncPaymentMethodStatusDataResponse {
                return AsyncPaymentMethodStatusDataResponse(
                    t.getString(ID_FIELD),
                    AsyncMethodStatus.valueOf(t.getString(STATUS_FIELD)),
                    t.getString(SOURCE_FIELD)
                )
            }
        }
    }
}

internal enum class AsyncMethodStatus {

    COMPLETE,
    PENDING,
    PROCESSING
}
