package io.primer.android.payment.billing

import io.primer.android.model.SyncValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

internal interface BillingAddressValidator {

    fun validate(
        billingAddressFields: Map<PrimerInputElementType, String?>,
        availableFields: Map<String, Boolean>
    ): List<SyncValidationError>
}
