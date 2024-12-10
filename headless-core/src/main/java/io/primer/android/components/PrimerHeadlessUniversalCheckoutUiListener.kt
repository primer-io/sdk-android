package io.primer.android.components

interface PrimerHeadlessUniversalCheckoutUiListener {

    /**
     * Called when SDK starts preparing to tokenize the payment method.
     * @param paymentMethodType payment method type.
     */
    fun onPreparationStarted(paymentMethodType: String) = Unit

    /**
     * Called when UI of payment method will be presented to the user.
     * @param paymentMethodType payment method type that will be showed.
     */
    fun onPaymentMethodShowed(paymentMethodType: String) = Unit
}
