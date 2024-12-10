package io.primer.android.paymentmethods

import io.primer.android.components.domain.error.PrimerInputValidationError

fun interface PaymentInputTypeValidator<T> {

    suspend fun validate(input: T?): PrimerInputValidationError?
}
