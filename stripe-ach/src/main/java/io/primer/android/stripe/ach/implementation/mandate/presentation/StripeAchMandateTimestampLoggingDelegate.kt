package io.primer.android.stripe.ach.implementation.mandate.presentation

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.core.extensions.toIso8601String
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

internal class StripeAchMandateTimestampLoggingDelegate(
    private val logReporter: LogReporter,
    private val analyticsInteractor: AnalyticsInteractor,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun logTimestamp(
        stripePaymentIntentId: String,
        date: Date,
    ): Unit =
        withContext(dispatcher) {
            supervisorScope {
                val message =
                    "Stripe ACH mandate for payment intent with id " +
                        "'$stripePaymentIntentId' was approved at '${date.toIso8601String()}'"
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
