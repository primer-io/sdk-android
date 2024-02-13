package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.retailerOutlet

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.paymentMethods.raw.validation.PaymentInputTypeValidator
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository

internal class XenditRetailerOutletValidator(
    private val retailOutletRepository: RetailOutletRepository
) : PaymentInputTypeValidator<String> {

    override suspend fun validate(input: String?): PrimerInputValidationError? {
        if (input.isNullOrBlank() || retailOutletRepository.getSelectedRetailOutlet() == null) {
            return PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet cannot be blank.",
                PrimerInputElementType.RETAIL_OUTLET
            )
        } else if (retailOutletRepository.getSelectedRetailOutlet()?.id != input) {
            return PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet ID can be only from list of retailers.",
                PrimerInputElementType.RETAIL_OUTLET
            )
        }
        return null
    }
}
