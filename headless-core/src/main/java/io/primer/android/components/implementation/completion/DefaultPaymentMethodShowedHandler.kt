package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.payments.core.helpers.PaymentMethodShowedHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DefaultPaymentMethodShowedHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
) : PaymentMethodShowedHandler {
    private val _paymentMethodShowed = MutableSharedFlow<String>()

    override val paymentMethodShowed: Flow<String> = _paymentMethodShowed

    override suspend fun handle(paymentMethodType: String) {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.ON_PAYMENT_METHOD_SHOWED,
                mapOf(
                    HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                        to paymentMethodType,
                ),
            ),
        )
        _paymentMethodShowed.emit(paymentMethodType)
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            val checkoutListener = PrimerHeadlessUniversalCheckout.instance.uiListener

            checkoutListener?.onPaymentMethodShowed(paymentMethodType)
        }
    }
}
