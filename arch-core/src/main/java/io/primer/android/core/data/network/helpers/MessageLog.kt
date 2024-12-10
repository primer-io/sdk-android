package io.primer.android.core.data.network.helpers

enum class Severity {
    INFO, WARN, ERROR
}

data class MessageLog(val message: String, val severity: Severity)
