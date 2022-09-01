package io.primer.android.events

enum class CheckoutEventType {
    TOKENIZE_SUCCESS,
    TOKEN_ADDED_TO_VAULT,
    RESUME_PENDING,
    RESUME_SUCCESS,
    RESUME_SUCCESS_INTERNAL,
    EXIT,
    SHOW_ERROR,
    SHOW_SUCCESS,
    DISMISS_INTERNAL,
    START_3DS,
    START_ASYNC_REDIRECT_FLOW,
    START_ASYNC_FLOW,
    START_VOUCHER_FLOW,
    PAYMENT_STARTED,
    PAYMENT_SUCCESS,
    PAYMENT_CONTINUE,
    PAYMENT_CONTINUE_HUC,
    CLIENT_SESSION_UPDATE_STARTED,
    CLIENT_SESSION_UPDATE_SUCCESS,
    CONFIGURATION_SUCCESS,
    TOKENIZE_STARTED,
    PREPARATION_STARTED,
    PAYMENT_METHOD_PRESENTED,
    CHECKOUT_AUTO_ERROR,
    CHECKOUT_MANUAL_ERROR,
    HUC_VALIDATION_ERROR,
    HUC_METADATA_CHANGED
}
