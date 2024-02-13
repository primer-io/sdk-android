package io.primer.android.components.domain.payments.paymentMethods.raw.validation

import io.primer.android.components.domain.error.PrimerInputValidationError

internal interface PaymentInputTypeValidator<T> {

    suspend fun validate(input: T?): PrimerInputValidationError?
}
