package io.primer.android.components.implementation.completion

import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DefaultCheckoutAdditionalInfoHandler(
    private val analyticsRepository: AnalyticsRepository,
    private val config: PrimerConfig,
    private val paymentResultRepository: PaymentResultRepository,
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main
) : CheckoutAdditionalInfoHandler {
    private val _checkoutAdditionalInfo = MutableSharedFlow<PrimerCheckoutAdditionalInfo>()
    override val checkoutAdditionalInfo: Flow<PrimerCheckoutAdditionalInfo> = _checkoutAdditionalInfo

    override suspend fun handle(checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo) {
        _checkoutAdditionalInfo.emit(checkoutAdditionalInfo)
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            val checkoutListener = PrimerHeadlessUniversalCheckout.instance.checkoutListener

            if (checkoutAdditionalInfo.completesCheckout) {
                if (config.settings.paymentHandling == PrimerPaymentHandling.MANUAL) {
                    analyticsRepository.addEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_PENDING
                        )
                    )
                    checkoutListener?.onResumePending(checkoutAdditionalInfo)
                } else {
                    analyticsRepository.addEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_COMPLETED
                        )
                    )
                    val paymentResult = paymentResultRepository.getPaymentResult()
                    checkoutListener?.onCheckoutCompleted(
                        PrimerCheckoutData(paymentResult.payment, checkoutAdditionalInfo)
                    )
                }
            } else {
                analyticsRepository.addEvent(
                    SdkFunctionParams(
                        HeadlessUniversalCheckoutAnalyticsConstants
                            .ON_CHECKOUT_ADDITIONAL_INFO_RECEIVED
                    )
                )
                checkoutListener?.onCheckoutAdditionalInfoReceived(checkoutAdditionalInfo)
            }
        }
    }
}
