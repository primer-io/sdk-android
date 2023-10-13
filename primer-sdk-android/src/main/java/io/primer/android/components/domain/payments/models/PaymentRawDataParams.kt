package io.primer.android.components.domain.payments.models

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.domain.base.Params

internal class PaymentRawDataParams(
    val paymentMethodType: String,
    val inputData: PrimerRawData
) : Params
