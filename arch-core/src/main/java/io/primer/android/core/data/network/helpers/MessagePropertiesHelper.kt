package io.primer.android.core.data.network.helpers

enum class MessageTypeHelper {
    RETRY,
    RETRY_SUCCESS,
    RETRY_FAILED
}

data class MessagePropertiesHelper(
    val messageTypeHelper: MessageTypeHelper,
    val message: String,
    val severity: Severity
)
