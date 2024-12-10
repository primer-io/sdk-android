package io.primer.android.paymentmethods.analytics.delegate

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.domain.error.models.PrimerError

class SdkAnalyticsErrorLoggingDelegate(
    private val analyticsInteractor: AnalyticsInteractor
) {
    suspend fun logSdkAnalyticsErrors(error: PrimerError) = analyticsInteractor(
        MessageAnalyticsParams(
            messageType = MessageType.ERROR,
            message = error.description,
            severity = Severity.ERROR,
            diagnosticsId = error.diagnosticsId,
            context = error.context
        )
    )
}
