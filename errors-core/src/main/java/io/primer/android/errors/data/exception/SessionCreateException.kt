package io.primer.android.errors.data.exception

class SessionCreateException(
    val paymentMethodType: String,
    val diagnosticsId: String?,
    val description: String?
) : Exception()
