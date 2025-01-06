package io.primer.android.core.data.network.helpers

enum class SeverityHelper {
    INFO,
    WARN,
    ERROR,
}

data class MessageLog(val message: String, val severity: SeverityHelper)
