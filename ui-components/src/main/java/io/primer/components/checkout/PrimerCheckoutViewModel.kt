package io.primer.components.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import io.primer.android.completion.PrimerHeadlessUniversalCheckoutResumeDecisionHandler
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.models.paymentMethods.PaymentMethod
import io.primer.components.ui.card.CardPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PrimerCheckoutViewModel : ViewModel(),
    PrimerCheckoutScope, PrimerHeadlessUniversalCheckoutListener {

    private val headlessUniversalCheckout = PrimerHeadlessUniversalCheckout.current

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Loading)
    override val state = _state.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod<*>>>(emptyList())

    override fun selectPaymentMethod(method: PaymentMethod<*>) {
        _state.value = PrimerCheckoutScope.State.Selected(method)
        // TODO: call action interactor conditionally
    }

    override fun clearSelectedPaymentMethod() {
        _state.value = PrimerCheckoutScope.State.Ready(_paymentMethods.value)
    }

    internal fun start(context: Context, clientToken: String, primerSettings: PrimerSettings) {
        headlessUniversalCheckout.start(
            context = context,
            clientToken = clientToken,
            settings = primerSettings,
            checkoutListener = this@PrimerCheckoutViewModel
        )
    }

    override fun onAvailablePaymentMethodsLoaded(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        _paymentMethods.value = paymentMethods.mapNotNull {
            when (it.paymentMethodType) {
                PaymentMethodType.PAYMENT_CARD.name -> CardPaymentMethod()
                else -> null
            }
        }
        _state.value = PrimerCheckoutScope.State.Ready(_paymentMethods.value)
    }

    override fun onTokenizationStarted(paymentMethodType: String) {
        // TODO
    }

    override fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) {
        // TODO
    }

    override fun onCheckoutResume(
        resumeToken: String,
        decisionHandler: PrimerHeadlessUniversalCheckoutResumeDecisionHandler
    ) {
        // TODO
    }

    override fun onFailed(error: PrimerError) {
        // TODO
    }

    override fun onFailed(error: PrimerError, checkoutData: PrimerCheckoutData?) {
        // TODO
    }

    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) {
        // TODO
    }

    override fun onCleared() {
        super.onCleared()
        headlessUniversalCheckout.cleanup()
    }
}
