package io.primer.android.webRedirectShared.implementation.composer.presentation.delegate

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.UUID

internal class WebRedirectLoggingDelegate(
    private val logReporter: LogReporter,
    private val analyticsInteractor: AnalyticsInteractor,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun logError(
        error: PrimerError,
        paymentMethodType: String,
    ): Unit =
        withContext(dispatcher) {
            supervisorScope {
                launch {
                    analyticsInteractor(
                        MessageAnalyticsParams(
                            messageType = MessageType.ERROR,
                            message = "$paymentMethodType: ${error.description}",
                            severity = Severity.ERROR,
                            diagnosticsId = error.diagnosticsId,
                            context =
                            ErrorContextParams(
                                errorId = error.errorId,
                                paymentMethodType = paymentMethodType,
                            ),
                        ),
                    )
                }
                launch {
                    logReporter.error(error.description)
                }
            }
        }

    suspend fun logStep(
        webRedirectStep: WebRedirectStep,
        paymentMethodType: String,
    ): Unit =
        withContext(dispatcher) {
            supervisorScope {
                val message =
                    when (webRedirectStep) {
                        WebRedirectStep.Loading -> "Web redirect is loading for '$paymentMethodType'"
                        WebRedirectStep.Loaded -> "Web redirect has loaded for '$paymentMethodType'"
                        WebRedirectStep.Dismissed ->
                            "Payment for '$paymentMethodType' was dismissed by user"

                        WebRedirectStep.Success -> "Payment for '$paymentMethodType' was successful"
                    }
                launch {
                    analyticsInteractor(
                        MessageAnalyticsParams(
                            messageType = MessageType.INFO,
                            message = message,
                            severity = Severity.INFO,
                            diagnosticsId = UUID.randomUUID().toString(),
                        ),
                    )
                }
                launch {
                    logReporter.info(message = message)
                }
            }
        }
}
