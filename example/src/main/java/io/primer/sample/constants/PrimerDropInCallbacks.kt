package io.primer.sample.constants

internal object PrimerDropInCallbacks {

    const val ON_UNIVERSAL_CHECKOUT_CLICKED = "openUniversalCheckoutClicked()"
    const val ON_VAULT_MANAGER_CLICKED = "openVaultManagerClicked()"
    const val ON_SHOW_PAYMENT_METHOD_BUTTON_CLICKED = "showPaymentMethodButtonClicked()"

    const val ON_BEFORE_PAYMENT_CREATED = "onBeforePaymentCreated(paymentMethodData, decisionHandler)"
    const val ON_FAILED_WITH_CHECKOUT_DATA = "onFailed(error, checkoutData?, errorHandler?)"
    const val ON_CHECKOUT_COMPLETED = "onCheckoutCompleted(checkoutData)"
    const val ON_TOKENIZE_SUCCESS = "onTokenizeSuccess(paymentMethodTokenData, decisionHandler)"
    const val ON_BEFORE_CLIENT_SESSION_UPDATED = "onBeforeClientSessionUpdated()"
    const val ON_CLIENT_SESSION_UPDATED = "onClientSessionUpdated(clientSession)"

    const val ON_DISMISSED = "onDismiss()"
}
