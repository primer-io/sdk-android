package io.primer.android.components.implementation.domain.handler

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

internal class AvailablePaymentMethodsHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main
) {
    fun invoke(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            analyticsRepository.addEvent(
                SdkFunctionParams(
                    HeadlessUniversalCheckoutAnalyticsConstants
                        .ON_AVAILABLE_PAYMENT_METHODS_LOADED,
                    mapOf(
                        HeadlessUniversalCheckoutAnalyticsConstants
                            .AVAILABLE_PAYMENT_METHODS_PARAM to paymentMethods.toString()
                    )
                )
            )

            PrimerHeadlessUniversalCheckout.instance.checkoutListener?.onAvailablePaymentMethodsLoaded(
                paymentMethods = paymentMethods
            )
        }
    }
}
