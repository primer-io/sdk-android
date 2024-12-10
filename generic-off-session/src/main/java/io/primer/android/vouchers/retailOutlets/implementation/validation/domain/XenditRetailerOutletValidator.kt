package io.primer.android.vouchers.retailOutlets.implementation.validation.domain

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.paymentmethods.PaymentInputTypeValidator
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository

internal class XenditRetailerOutletValidator(
    private val retailOutletRepository: RetailOutletRepository
) : PaymentInputTypeValidator<String> {

    override suspend fun validate(input: String?): PrimerInputValidationError? {
        if (input.isNullOrBlank()) {
            return PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet cannot be blank.",
                PrimerInputElementType.RETAIL_OUTLET
            )
        } else if (retailOutletRepository.getCachedRetailOutlets().none { it.id == input }) {
            return PrimerInputValidationError(
                "invalid-retailer",
                "[invalid-retailer] Retailer outlet ID can be only from list of retailers.",
                PrimerInputElementType.RETAIL_OUTLET
            )
        }
        return null
    }
}
