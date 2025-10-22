package io.primer.android.components.analytics.data.model

sealed interface EventType {
    val eventName: String

    // Optional context with defaults
    val paymentMethod: String? get() = null
    val paymentId: String? get() = null
    val redirectDestinationUrl: String? get() = null
    val threedsProvider: String? get() = null
    val threedsResponse: String? get() = null

    /**
     * Triggered when the SDK starts to initialize, and we're start calling out BE services.
     * (CheckoutViewModel)
     */
    data object SdkInitStart : EventType {
        override val eventName = "SDK_INIT_START"
    }

    /**
     * Triggered when the SDK init is finished, and we have a fully loaded checkout SDK that can be used.
     * i.e. we have all the API calls we need in order to present the checkout.
     * (CheckoutViewModel)
     */
    data object SdkInitEnd : EventType {
        override val eventName = "SDK_INIT_END"
    }

    /**
     * Triggered when a user can interact with the checkout, typically after all API Calls are finished
     * and the UI is rendered using the components or headless
     * (PaymentMethodSelectionViewModel)
     */
    data object CheckoutFlowStarted : EventType {
        override val eventName = "CHECKOUT_FLOW_STARTED"
    }

    /**
     * Triggered when the user exits the payment flow without completing the payment,
     * either voluntarily or due to a timeout.
     * (CheckoutViewModel)
     */
    data object PaymentFlowExited : EventType {
        override val eventName = "PAYMENT_FLOW_EXITED"
    }

    /**
     * Logged when the user attempts to re-submit payment after a failure.
     * (CheckoutViewModel)
     */
    data object PaymentReattempted : EventType {
        override val eventName = "PAYMENT_REATTEMPTED"
    }

    /**
     * Logged when the user chooses a payment method (e.g., credit card, PayPal, bank transfer).
     * (PaymentMethodSelectionViewModel)
     */
    data class PaymentMethodSelection(
        override val paymentMethod: String,
    ) : EventType {
        override val eventName = "PAYMENT_METHOD_SELECTION"
    }

    /**
     * Triggered when the user inputs payment information (e.g., card details or account credentials).
     * (CardFormViewModel)
     */
    data class PaymentDetailsEntered(
        override val paymentMethod: String,
    ) : EventType {
        override val eventName = "PAYMENT_DETAILS_ENTERED"

        companion object {
            fun card() = PaymentDetailsEntered("PAYMENT_CARD")
        }
    }

    /**
     * Logged when the user clicks "Pay" or equivalent to submit their payment details for processing.
     * (CardFormViewModel)
     */
    data class PaymentSubmitted(
        override val paymentMethod: String,
    ) : EventType {
        override val eventName = "PAYMENT_SUBMITTED"

        companion object {
            fun card() = PaymentSubmitted("PAYMENT_CARD")
        }
    }

    /**
     * Triggered when the payment gateway begins processing the transaction:
     * - When the PAY button is triggered for card payments
     * - When the APM button was clicked and we called our Primer Backend for next steps.
     * (CardFormViewModel)
     */
    data class PaymentProcessingStarted(
        override val paymentMethod: String,
    ) : EventType {
        override val eventName = "PAYMENT_PROCESSING_STARTED"

        companion object {
            fun card() = PaymentProcessingStarted("PAYMENT_CARD")
        }
    }

    /**
     * Logged when the payment is processed successfully, and the transaction is complete.
     * (CardFormViewModel)
     */
    data class PaymentSuccess(
        override val paymentMethod: String,
        override val paymentId: String,
    ) : EventType {
        override val eventName = "PAYMENT_SUCCESS"

        companion object {
            fun card(paymentId: String) = PaymentSuccess("PAYMENT_CARD", paymentId)
        }
    }

    /**
     * Triggered when the payment processing fails due to issues like insufficient funds,
     * incorrect details, or system errors.
     * (CardFormViewModel)
     */
    data class PaymentFailure(
        override val paymentMethod: String,
        override val paymentId: String? = null,
    ) : EventType {
        override val eventName = "PAYMENT_FAILURE"

        companion object {
            fun card(paymentId: String? = null) = PaymentFailure("PAYMENT_CARD", paymentId)
        }
    }

    /**
     * Triggered when the user is required to complete a 3D Secure authentication process,
     * such as entering a one-time password (OTP) or verifying their identity with their bank.
     * This event is logged when the authentication page is loaded.
     * (not implemented now because it needs to be triggered from outside components sdk)
     */
    data class PaymentThreeDS(
        override val paymentMethod: String,
        override val threedsProvider: String,
        override val threedsResponse: String? = null,
    ) : EventType {
        override val eventName = "PAYMENT_THREEDS"
    }

    /**
     * Triggered when the user is redirected from the primary payment page to a third-party site
     * for additional processing, such as entering credentials or completing payment authorization.
     * (not implemented now because we don't have yet redirects)
     */
    data class PaymentRedirect(
        override val paymentMethod: String,
        override val redirectDestinationUrl: String,
    ) : EventType {
        override val eventName = "PAYMENT_REDIRECT_TO_THIRD_PARTY"
    }
}
