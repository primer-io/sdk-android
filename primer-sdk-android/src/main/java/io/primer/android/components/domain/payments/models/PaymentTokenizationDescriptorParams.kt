package io.primer.android.components.domain.payments.models

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.domain.base.Params
import io.primer.android.model.dto.PaymentMethodType

internal data class PaymentTokenizationDescriptorParams(
    val paymentMethodType: PaymentMethodType,
    val inputData: PrimerHeadlessUniversalCheckoutInputData
) : Params
