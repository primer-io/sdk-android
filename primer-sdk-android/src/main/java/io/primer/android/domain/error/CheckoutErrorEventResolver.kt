package io.primer.android.domain.error

import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.completion.CheckoutErrorHandler
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PaymentHandling
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.ui.fragments.ErrorType

internal class CheckoutErrorEventResolver(
    analyticsRepository: AnalyticsRepository,
    errorMapperFactory: ErrorMapperFactory,
    private val settings: PrimerSettings,
    private val eventDispatcher: EventDispatcher
) : BaseErrorEventResolver(errorMapperFactory, analyticsRepository) {

    override fun dispatch(error: PrimerError) {
        val checkoutErrorHandler = object : CheckoutErrorHandler {
            override fun showErrorMessage(message: String?) {
                eventDispatcher.dispatchEvent(
                    CheckoutEvent.ShowError(
                        errorType = ErrorType.PAYMENT_FAILED,
                        message = message
                    )
                )
            }
        }
        when (settings.options.paymentHandling) {
            PaymentHandling.AUTO -> eventDispatcher.dispatchEvent(
                CheckoutEvent.CheckoutPaymentError(
                    error,
                    errorHandler = checkoutErrorHandler
                )
            )
            PaymentHandling.MANUAL -> eventDispatcher.dispatchEvent(
                CheckoutEvent.CheckoutError(error, checkoutErrorHandler)
            )
        }
    }
}
