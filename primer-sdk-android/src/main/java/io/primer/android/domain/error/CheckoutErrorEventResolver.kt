package io.primer.android.domain.error

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.models.PaymentMethodError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.ui.fragments.ErrorType

internal class CheckoutErrorEventResolver(
    analyticsRepository: AnalyticsRepository,
    errorMapperFactory: ErrorMapperFactory,
    logReporter: LogReporter,
    private val settings: PrimerSettings,
    private val eventDispatcher: EventDispatcher
) : BaseErrorEventResolver(errorMapperFactory, analyticsRepository, logReporter) {

    override fun dispatch(error: PrimerError) {
        val checkoutErrorHandler = object : PrimerErrorDecisionHandler {
            override fun showErrorMessage(errorMessage: String?) {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ShowError(
                        errorType = if (error is PaymentMethodError.PaymentMethodCancelledError) {
                            ErrorType.PAYMENT_CANCELLED
                        } else {
                            ErrorType.PAYMENT_FAILED
                        },
                        message = errorMessage
                    )
                )
            }
        }
        when (settings.paymentHandling) {
            PrimerPaymentHandling.AUTO -> eventDispatcher.dispatchEvent(
                CheckoutEvent.CheckoutPaymentError(
                    error,
                    errorHandler = checkoutErrorHandler
                )
            )

            PrimerPaymentHandling.MANUAL -> eventDispatcher.dispatchEvent(
                CheckoutEvent.CheckoutError(error, checkoutErrorHandler)
            )
        }
    }
}
