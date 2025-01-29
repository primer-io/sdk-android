package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.payments.core.helpers.PreparationStartHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DefaultPreparationStartHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
) : PreparationStartHandler {
    private val _startPreparation = MutableSharedFlow<String>()
    override val preparationStarted: Flow<String> = _startPreparation

    override suspend fun handle(paymentMethodType: String) {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.ON_PREPARATION_STARTED,
                mapOf(
                    HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                        to paymentMethodType,
                ),
            ),
        )
        _startPreparation.emit(paymentMethodType)
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            val checkoutListener = PrimerHeadlessUniversalCheckout.instance.uiListener

            checkoutListener?.onPreparationStarted(paymentMethodType)
        }
    }
}
