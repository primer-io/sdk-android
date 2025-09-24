package io.primer.android.components.analytics.data.model

enum class EventType(val value: String) {
    /**
     * ❌ Triggered when the SDK starts to initialize, and we're start calling out BE services.
     * (commented out in CheckoutLoader)
     */
    SDK_INIT_START("SDK_INIT_START"),

    /**
     * ❌ Triggered when the SDK init is finished, and we have a fully loaded checkout SDK that can be used.
     * i.e. we have all the API calls we need in order to present the checkout.
     * (commented out in CheckoutLoader)
     */
    SDK_INIT_END("SDK_INIT_END"),

    /**
     * ❌ Triggered when a user can interact with the checkout, typically after all API Calls are finished
     * and the UI is rendered using the components or headless
     * (CheckoutViewModel, not working because of DI)
     */
    CHECKOUT_FLOW_STARTED("CHECKOUT_FLOW_STARTED"),

    /**
     * ✅ Logged when the user chooses a payment method (e.g., credit card, PayPal, bank transfer).
     * (PaymentMethodSelectionViewModel)
     */
    PAYMENT_METHOD_SELECTION("PAYMENT_METHOD_SELECTION"),

    /**
     * ✅ Triggered when the user inputs payment information (e.g., card details or account credentials).
     * (CardFormViewModel)
     */
    PAYMENT_DETAILS_ENTERED("PAYMENT_DETAILS_ENTERED"),

    /**
     * ✅ Logged when the user clicks "Pay" or equivalent to submit their payment details for processing.
     * (CardFormViewModel)
     */
    PAYMENT_SUBMITTED("PAYMENT_SUBMITTED"),

    /**
     * ✅ Triggered when the payment gateway begins processing the transaction:
     * - When the PAY button is triggered for card payments
     * - When the APM button was clicked and we called our Primer Backend for next steps.
     * (CardFormViewModel)
     */
    PAYMENT_PROCESSING_STARTED("PAYMENT_PROCESSING_STARTED"),

    /**
     * ❌ Triggered when the user is redirected from the primary payment page to a third-party site
     * for additional processing, such as entering credentials or completing payment authorization.
     * (not implemented now because we don't have yet redirects)
     */
    PAYMENT_REDIRECT_TO_THIRD_PARTY("PAYMENT_REDIRECT_TO_THIRD_PARTY"),

    /**
     * ❌ Triggered when the user is required to complete a 3D Secure authentication process,
     * such as entering a one-time password (OTP) or verifying their identity with their bank.
     * This event is logged when the authentication page is loaded.
     * (not implemented now because it needs to be triggered from outside components sdk)
     */
    PAYMENT_THREEDS("PAYMENT_THREEDS"),

    /**
     * ✅ Logged when the payment is processed successfully, and the transaction is complete.
     * (CardFormViewModel)
     */
    PAYMENT_SUCCESS("PAYMENT_SUCCESS"),

    /**
     * ✅ Triggered when the payment processing fails due to issues like insufficient funds,
     * incorrect details, or system errors.
     * (CardFormViewModel)
     */
    PAYMENT_FAILURE("PAYMENT_FAILURE"),

    /**
     * ❌ Logged when the user attempts to re-submit payment after a failure.
     * (CheckoutViewModel, not working because of DI)
     */
    PAYMENT_REATTEMPTED("PAYMENT_REATTEMPTED"),

    /**
     * ❌ Triggered when the user exits the payment flow without completing the payment,
     * either voluntarily or due to a timeout.
     * (CheckoutViewModel, not working because of DI)
     */
    PAYMENT_FLOW_EXITED("PAYMENT_FLOW_EXITED"),
}
