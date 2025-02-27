package io.primer.android.payments.core.status.data.models

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer

internal data class AsyncPaymentMethodStatusDataResponse(
    val id: String,
    val status: AsyncMethodStatus,
    val source: String,
) : JSONDeserializable {
    companion object {
        private const val ID_FIELD = "id"
        private const val STATUS_FIELD = "status"
        private const val SOURCE_FIELD = "source"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                AsyncPaymentMethodStatusDataResponse(
                    t.getString(ID_FIELD),
                    AsyncMethodStatus.valueOf(t.getString(STATUS_FIELD)),
                    t.getString(SOURCE_FIELD),
                )
            }
    }
}

internal enum class AsyncMethodStatus {
    COMPLETE,
    PENDING,
    PROCESSING,
}
