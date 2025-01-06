package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DefaultCheckoutSuccessHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main,
) : CheckoutSuccessHandler {
    private val _checkoutCompleted = MutableSharedFlow<Payment>()
    override val checkoutCompleted: Flow<Payment> = _checkoutCompleted

    override suspend fun handle(
        payment: Payment,
        additionalInfo: PrimerCheckoutAdditionalInfo?,
    ) {
        analyticsRepository.addEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_COMPLETED,
            ),
        )
        _checkoutCompleted.emit(payment)
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            val checkoutListener = PrimerHeadlessUniversalCheckout.instance.checkoutListener

            checkoutListener?.onCheckoutCompleted(
                PrimerCheckoutData(
                    payment = payment,
                    additionalInfo = additionalInfo,
                ),
            )
        }
    }
}
