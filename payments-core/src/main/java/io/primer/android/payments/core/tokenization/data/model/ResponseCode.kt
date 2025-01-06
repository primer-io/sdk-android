package io.primer.android.payments.core.tokenization.data.model

enum class ResponseCode {
    NOT_PERFORMED,
    SKIPPED,
    AUTH_SUCCESS,
    AUTH_FAILED,
    CHALLENGE,
    METHOD,
}
