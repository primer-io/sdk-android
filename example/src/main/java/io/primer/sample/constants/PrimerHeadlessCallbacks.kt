package io.primer.sample.constants

internal object PrimerHeadlessCallbacks {

    const val ON_BEFORE_PAYMENT_CREATED =
        "onBeforePaymentCreated(paymentMethodData, decisionHandler)"
    const val ON_FAILED_WITH_CHECKOUT_DATA = "onFailed(error, checkoutData?)"
    const val ON_FAILED_WITHOUT_CHECKOUT_DATA = "onFailed(error)"
    const val ON_CHECKOUT_COMPLETED = "onCheckoutCompleted(checkoutData)"
    const val ON_TOKENIZE_SUCCESS = "onTokenizeSuccess(paymentMethodTokenData, decisionHandler)"
    const val ON_CHECKOUT_RESUME = "onCheckoutResume(resumeToken, decisionHandler)"
    const val ON_RESUME_PENDING = "onResumePending(additionalInfo)"
    const val ON_CHECKOUT_ADDITIONAL_INFO_RECEIVED = "onCheckoutAdditionalInfoReceived(additionalInfo)"
    const val ON_BEFORE_CLIENT_SESSION_UPDATED = "onBeforeClientSessionUpdated()"
    const val ON_CLIENT_SESSION_UPDATED = "onClientSessionUpdated(clientSession)"
    const val ON_PREPARATION_STARTED = "onPreparationStarted(paymentMethodType)"
    const val ON_PAYMENT_METHOD_SHOWED = "onPaymentMethodShowed(paymentMethodType)"
    const val ON_TOKENIZATION_STARTED = "onTokenizationStarted(paymentMethodType)"
}
