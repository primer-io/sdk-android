package io.primer.android.payment.billing_address

import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.model.dto.SyncValidationError

internal interface IBillingAddressValidator {

    fun validate(
        billingAddressFields: Map<PrimerInputFieldType, String?>,
        availableFields: Map<String, Boolean>
    ): List<SyncValidationError>
}
