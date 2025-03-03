package io.primer.components.ui.components.redirect

import io.primer.components.Primer
import io.primer.components.ui.checkout.PrimerPaymentMethodViewModel

internal class KlarnaViewModel: PrimerPaymentMethodViewModel(), Primer.Scope.PaymentMethod.Klarna {

    override fun foo(): Boolean {
        return true
    }

    override fun bar(): Boolean {
        return false
    }

}
