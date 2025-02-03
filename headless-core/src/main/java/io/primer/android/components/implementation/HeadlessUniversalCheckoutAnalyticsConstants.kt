package io.primer.android.components.implementation

internal object HeadlessUniversalCheckoutAnalyticsConstants {
    // PrimerHeadlessUniversalCheckout
    const val START_METHOD = "PrimerHeadlessUniversalCheckout.start"
    const val SET_CHECKOUT_LISTENER_METHOD = "PrimerHeadlessUniversalCheckout.setCheckoutListener"
    const val SET_CHECKOUT_UI_LISTENER_METHOD =
        "PrimerHeadlessUniversalCheckout.setCheckoutUiListener"
    const val CLEANUP_METHOD = "PrimerHeadlessUniversalCheckout.cleanup"

    const val SETTINGS_PARAM = "sdkSettings"
    const val CLIENT_TOKEN_PARAM = "clientToken"

    // PrimerHeadlessUniversalCheckoutListener
    const val ON_BEFORE_PAYMENT_COMPLETED = "PrimerHeadlessUniversalCheckout.onBeforePaymentCreated"
    const val ON_AVAILABLE_PAYMENT_METHODS_LOADED =
        "PrimerHeadlessUniversalCheckout.onAvailablePaymentMethodsLoaded"
    const val ON_CHECKOUT_COMPLETED = "PrimerHeadlessUniversalCheckout.onCheckoutCompleted"
    const val ON_CHECKOUT_FAILED = "PrimerHeadlessUniversalCheckout.onFailed"
    const val ON_TOKENIZE_SUCCESS = "PrimerHeadlessUniversalCheckout.onTokenizeSuccess"
    const val ON_CHECKOUT_RESUME = "PrimerHeadlessUniversalCheckout.onCheckoutResume"
    const val ON_CHECKOUT_PENDING = "PrimerHeadlessUniversalCheckout.onResumePending"
    const val ON_CHECKOUT_ADDITIONAL_INFO_RECEIVED =
        "PrimerHeadlessUniversalCheckout.onCheckoutAdditionalInfoReceived"
    const val ON_CLIENT_SESSION_LOADED = "PrimerHeadlessUniversalCheckout.onClientSessionLoaded"
    const val ON_CLIENT_SESSION_UPDATED = "PrimerHeadlessUniversalCheckout.onClientSessionUpdated"
    const val ON_BEFORE_CLIENT_SESSION_UPDATED =
        "PrimerHeadlessUniversalCheckout.onBeforeClientSessionUpdated"
    const val ON_TOKENIZATION_STARTED = "PrimerHeadlessUniversalCheckout.onTokenizationStarted"

    // PrimerHeadlessUniversalCheckoutUiListener
    const val ON_PREPARATION_STARTED = "PrimerHeadlessUniversalUiCheckout.onPreparationStarted"
    const val ON_PAYMENT_METHOD_SHOWED = "PrimerHeadlessUniversalUiCheckout.onPaymentMethodShowed"

    // Params
    const val AVAILABLE_PAYMENT_METHODS_PARAM = "availablePaymentMethods"
    const val ERROR_ID_PARAM = "errorId"
    const val ERROR_DESCRIPTION_PARAM = "errorDescription"
    const val PAYMENT_METHOD_TYPE = "paymentMethodType"
}
