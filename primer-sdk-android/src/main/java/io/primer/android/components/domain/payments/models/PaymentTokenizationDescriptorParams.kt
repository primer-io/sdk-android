package io.primer.android.components.domain.payments.models

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.domain.base.Params

internal data class PaymentTokenizationDescriptorParams(
    val paymentMethodType: PrimerPaymentMethodType,
    val inputData: PrimerHeadlessUniversalCheckoutInputData
) : Params
