package io.primer.android.components.presentation.paymentMethods.analytics.delegate

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.components.domain.error.PrimerValidationError
import kotlinx.coroutines.flow.collect

internal class SdkAnalyticsValidationErrorLoggingDelegate(
    private val analyticsInteractor: AnalyticsInteractor
) {
    suspend fun logSdkAnalyticsErrors(error: PrimerValidationError) = analyticsInteractor(
        MessageAnalyticsParams(
            messageType = MessageType.VALIDATION_FAILED,
            message = error.description,
            severity = Severity.WARN,
            diagnosticsId = error.diagnosticsId,
            context = ErrorContextParams(errorId = error.errorId)
        )
    ).collect()
}
