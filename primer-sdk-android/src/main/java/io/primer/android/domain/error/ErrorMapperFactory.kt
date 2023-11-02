package io.primer.android.domain.error

internal enum class ErrorMapperType {
    ACTION_UPDATE,
    PAYMENT_CREATE,
    PAYMENT_RESUME,
    SESSION_CREATE,
    HUC,
    PAYMENT_METHODS,
    KLARNA,
    GOOGLE_PAY,
    APAYA,
    I_PAY88,
    THREE_DS,
    NOL_PAY,
    DEFAULT
}

internal interface ErrorMapperFactory {

    fun buildErrorMapper(type: ErrorMapperType): ErrorMapper
}
