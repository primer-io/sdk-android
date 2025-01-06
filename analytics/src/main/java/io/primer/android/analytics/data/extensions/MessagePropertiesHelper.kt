package io.primer.android.analytics.data.extensions

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.core.data.network.helpers.MessageTypeHelper
import io.primer.android.core.data.network.helpers.SeverityHelper

internal fun MessageTypeHelper.toMessageType() =
    when (this) {
        MessageTypeHelper.RETRY -> MessageType.RETRY
        MessageTypeHelper.RETRY_SUCCESS -> MessageType.RETRY_SUCCESS
        MessageTypeHelper.RETRY_FAILED -> MessageType.RETRY_FAILED
    }

internal fun SeverityHelper.toSeverity() =
    when (this) {
        SeverityHelper.INFO -> Severity.INFO
        SeverityHelper.WARN -> Severity.WARN
        SeverityHelper.ERROR -> Severity.ERROR
    }
