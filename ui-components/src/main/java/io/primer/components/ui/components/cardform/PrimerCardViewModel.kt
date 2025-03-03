package io.primer.components.ui.components.cardform

import io.primer.components.Primer
import io.primer.components.ui.checkout.PrimerPaymentMethodViewModel
import java.util.Date

internal class PrimerCardViewModel : PrimerPaymentMethodViewModel(), Primer.Scope.PaymentMethod.Card {

    override fun isInputValid(): Boolean {
        return false
    }

    override fun setCardNumber(input: String) {

    }

    override fun setCvv(input: String) {

    }

    override fun setExpiryDate(date: Date) {

    }

}
