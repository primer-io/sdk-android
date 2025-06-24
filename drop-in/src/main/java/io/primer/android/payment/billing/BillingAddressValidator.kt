package io.primer.android.payment.billing

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.ui.core.model.SyncValidationError

internal interface BillingAddressValidator {
    fun validate(
        billingAddressFields: Map<PrimerInputElementType, String?>,
        availableFields: Map<String, Boolean>,
    ): List<SyncValidationError>
}
