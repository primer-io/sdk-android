package io.primer.android.domain.action.models

import io.primer.android.data.configuration.models.PaymentMethodType

internal data class ActionUpdateSelectPaymentMethodParams(
    val paymentMethodType: PaymentMethodType,
    val cardNetwork: String? = null
) : BaseActionUpdateParams
