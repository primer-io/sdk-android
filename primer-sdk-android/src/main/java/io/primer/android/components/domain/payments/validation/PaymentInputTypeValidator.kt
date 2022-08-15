package io.primer.android.components.domain.payments.validation

import io.primer.android.components.domain.error.PrimerInputValidationError

internal interface PaymentInputTypeValidator<T> {

    fun validate(input: T?): PrimerInputValidationError?
}
