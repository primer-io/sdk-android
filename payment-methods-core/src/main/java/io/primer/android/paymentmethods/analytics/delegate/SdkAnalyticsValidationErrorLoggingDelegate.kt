package io.primer.android.paymentmethods.analytics.delegate

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.components.domain.error.PrimerValidationError

class SdkAnalyticsValidationErrorLoggingDelegate(
    private val analyticsInteractor: AnalyticsInteractor
) {
    suspend fun logSdkAnalyticsError(error: PrimerError) = logSdkAnalyticsError(
        description = error.description,
        diagnosticsId = error.diagnosticsId,
        errorId = error.errorId,
        context = error.context
    )

    suspend fun logSdkAnalyticsError(error: PrimerValidationError) = logSdkAnalyticsError(
        description = error.description,
        diagnosticsId = error.diagnosticsId,
        errorId = error.errorId
    )

    private suspend fun logSdkAnalyticsError(
        description: String,
        diagnosticsId: String,
        errorId: String,
        context: BaseContextParams? = null
    ) {
        analyticsInteractor(
            MessageAnalyticsParams(
                messageType = MessageType.VALIDATION_FAILED,
                message = description,
                severity = Severity.WARN,
                diagnosticsId = diagnosticsId,
                context = context ?: ErrorContextParams(errorId = errorId)
            )
        )
    }
}
