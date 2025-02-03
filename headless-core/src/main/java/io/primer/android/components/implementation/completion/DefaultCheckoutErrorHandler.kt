package io.primer.android.components.implementation.completion

import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class DefaultCheckoutErrorHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val config: PrimerConfig,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
) : CheckoutErrorHandler {
    private val _errors = MutableSharedFlow<PrimerError>()
    override val errors: Flow<PrimerError> = _errors

    override suspend fun handle(
        error: PrimerError,
        payment: Payment?,
    ) {
        _errors.emit(error)
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            analyticsRepository.apply {
                addEvent(
                    params = SdkFunctionParams(
                        HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_FAILED,
                        mapOf(
                            HeadlessUniversalCheckoutAnalyticsConstants.ERROR_ID_PARAM
                                to error.errorId,
                            HeadlessUniversalCheckoutAnalyticsConstants.ERROR_DESCRIPTION_PARAM
                                to error.description,
                        ),
                    ),
                )
                addEvent(
                    MessageAnalyticsParams(
                        messageType = MessageType.ERROR,
                        message = error.description,
                        severity = Severity.ERROR,
                        diagnosticsId = error.diagnosticsId,
                        context = error.context,
                    ),
                )
            }
            val checkoutListener = PrimerHeadlessUniversalCheckout.instance.checkoutListener
            when (config.settings.paymentHandling) {
                PrimerPaymentHandling.AUTO ->
                    checkoutListener?.onFailed(
                        error = error,
                        checkoutData =
                        payment?.let {
                            PrimerCheckoutData(
                                payment,
                            )
                        },
                    )

                PrimerPaymentHandling.MANUAL ->
                    checkoutListener?.onFailed(
                        error = error,
                    )
            }
        }
    }
}
