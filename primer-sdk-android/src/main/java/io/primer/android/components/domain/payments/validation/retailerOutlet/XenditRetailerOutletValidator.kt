package io.primer.android.components.domain.payments.validation.retailerOutlet

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.validation.PaymentInputTypeValidator
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository

internal class XenditRetailerOutletValidator(
    private val retailOutletRepository: RetailOutletRepository
) : PaymentInputTypeValidator<String> {
    override fun validate(input: String?): PrimerInputValidationError? {
        if (input.isNullOrBlank() || retailOutletRepository.getSelectedRetailOutlet() == null) {
            return PrimerInputValidationError(
                "invalid-retailer-outlet",
                "Retailer outlet can not be blank.",
                PrimerInputElementType.RETAIL_OUTLET
            )
        } else if (retailOutletRepository.getSelectedRetailOutlet()?.id != input) {
            return PrimerInputValidationError(
                "invalid-retailer-outlet",
                "Retailer outlet ID can be only from list of retailers.",
                PrimerInputElementType.RETAIL_OUTLET
            )
        }
        return null
    }
}
