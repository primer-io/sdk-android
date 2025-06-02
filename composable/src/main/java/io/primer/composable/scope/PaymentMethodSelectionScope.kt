package io.primer.composable.scope

import io.primer.composable.model.PrimerPaymentMethod

interface PaymentMethodSelectionScope {

    val paymentMethods: List<PrimerPaymentMethod>

    fun onPaymentMethodSelected(paymentMethod: PrimerPaymentMethod)

}
