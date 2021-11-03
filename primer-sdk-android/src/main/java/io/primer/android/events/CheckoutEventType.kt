package io.primer.android.events

enum class CheckoutEventType {
    TOKENIZE_SUCCESS,
    TOKENIZE_ERROR,
    TOKEN_ADDED_TO_VAULT,
    TOKEN_REMOVED_FROM_VAULT,
    RESUME_SUCCESS,
    RESUME_ERR0R,
    SAVED_PAYMENT_INSTRUMENT_FETCHED,
    EXIT,
    API_ERROR,
    SHOW_ERROR,
    TOGGLE_LOADING,
    SHOW_SUCCESS,
    DISMISS_INTERNAL,
    TOKEN_SELECTED,
    START_3DS,
    START_ASYNC_FLOW
}
