package io.primer.android.domain.error

internal enum class ErrorMapperType {
    ACTION_UPDATE,
    PAYMENT_CREATE,
    PAYMENT_RESUME,
    SESSION_CREATE,
    HUC,
    PAYMENT_METHODS,
    KLARNA,
    I_PAY88,
    DEFAULT
}

internal interface ErrorMapperFactory {

    fun buildErrorMapper(type: ErrorMapperType): ErrorMapper
}
