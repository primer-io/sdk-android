package io.primer.android.klarna.implementation.session.data.validation.validator

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory

internal object KlarnaPaymentCategoryValidator {
    fun validate(
        paymentCategories: List<KlarnaPaymentCategory>?,
        paymentCategory: KlarnaPaymentCategory
    ): PrimerValidationError? =
        if (paymentCategories == null) {
            PrimerValidationError(
                errorId = KlarnaValidations.SESSION_NOT_CREATED_ERROR_ID,
                description = "Session needs to be created before payment category can " +
                    "be collected."
            )
        } else if (paymentCategories.none { it == paymentCategory }) {
            PrimerValidationError(
                errorId = KlarnaValidations.INVALID_PAYMENT_CATEGORY_ERROR_ID,
                description = "Payment category is invalid."
            )
        } else {
            null
        }
}
