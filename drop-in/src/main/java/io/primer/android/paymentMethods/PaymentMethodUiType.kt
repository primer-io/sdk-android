package io.primer.android.paymentMethods

enum class PaymentMethodUiType {
    /**
     * The FORM type represents a payment method where the form is created and controlled by Primer
     */
    FORM,

    /**
     * The SIMPLE_BUTTON type represents a payment method where the customer only has to press
     * the button. Everything else is handled by some third party native library
     */
    SIMPLE_BUTTON,
}
