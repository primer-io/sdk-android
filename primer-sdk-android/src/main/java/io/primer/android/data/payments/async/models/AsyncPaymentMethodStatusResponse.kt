package io.primer.android.data.payments.async.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AsyncPaymentMethodStatusResponse(
    val id: String,
    val status: AsyncMethodStatus,
    val source: String
)

@Serializable
internal enum class AsyncMethodStatus {

    COMPLETE,
    PENDING,
    PROCESSING
}
