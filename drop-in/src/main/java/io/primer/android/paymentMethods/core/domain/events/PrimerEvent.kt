package io.primer.android.paymentMethods.core.domain.events

import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.paymentMethods.core.domain.PrimerEventsInteractor
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType

internal sealed interface PrimerEvent {
    data class AvailablePaymentMethodsLoaded(
        val paymentMethodsHolder: PrimerEventsInteractor.PaymentMethodsHolder,
    ) : PrimerEvent

    data class CheckoutFailed(val errorMessage: String?, val errorType: ErrorType) : PrimerEvent

    data class CheckoutCompleted(val checkoutData: PrimerCheckoutData?, val successType: SuccessType) : PrimerEvent

    data object Dismiss : PrimerEvent

    data object DisableDismiss : PrimerEvent
}
