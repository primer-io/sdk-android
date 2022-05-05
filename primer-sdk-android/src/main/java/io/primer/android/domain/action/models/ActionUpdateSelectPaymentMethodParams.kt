package io.primer.android.domain.action.models

import io.primer.android.model.dto.PaymentMethodType

internal data class ActionUpdateSelectPaymentMethodParams(
    val paymentMethodType: PaymentMethodType,
    val cardNetwork: String? = null
) : BaseActionUpdateParams
