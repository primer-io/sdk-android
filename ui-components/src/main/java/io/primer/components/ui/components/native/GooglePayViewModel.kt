package io.primer.components.ui.components.native

import io.primer.components.Primer
import io.primer.components.ui.checkout.PrimerPaymentMethodViewModel

internal class GooglePayViewModel : PrimerPaymentMethodViewModel(), Primer.Scope.PaymentMethod.GooglePay {

    override fun foo(): Boolean {
        return true
    }

    override fun bar(): Boolean {
        return false
    }

}
