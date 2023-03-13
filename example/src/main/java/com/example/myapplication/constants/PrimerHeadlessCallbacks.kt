package com.example.myapplication.constants

internal object PrimerHeadlessCallbacks {

    const val ON_BEFORE_PAYMENT_CREATED =
        "onBeforePaymentCreated(paymentMethodData, decisionHandler)"
    const val ON_FAILED_WITH_CHECKOUT_DATA = "onFailed(error, checkoutData?, errorHandler?)"
    const val ON_CHECKOUT_COMPLETED = "onCheckoutCompleted(checkoutData)"
    const val ON_TOKENIZE_SUCCESS = "onTokenizeSuccess(paymentMethodTokenData, decisionHandler)"
    const val ON_BEFORE_CLIENT_SESSION_UPDATED = "onBeforeClientSessionUpdated()"
    const val ON_CLIENT_SESSION_UPDATED = "onClientSessionUpdated(clientSession)"
    const val ON_PREPARATION_STARTED = "onPreparationStarted(paymentMethodType)"
    const val ON_TOKENIZATION_STARTED = "onTokenizationStarted(paymentMethodType)"
}
