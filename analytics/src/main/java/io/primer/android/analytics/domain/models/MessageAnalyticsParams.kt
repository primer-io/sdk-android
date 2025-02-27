package io.primer.android.analytics.domain.models

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity

data class MessageAnalyticsParams(
    val messageType: MessageType,
    val message: String,
    val severity: Severity,
    val diagnosticsId: String? = null,
    val context: BaseContextParams? = null,
) : BaseAnalyticsParams()
