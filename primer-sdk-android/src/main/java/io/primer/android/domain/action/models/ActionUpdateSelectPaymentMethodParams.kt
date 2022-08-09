package io.primer.android.domain.action.models

internal data class ActionUpdateSelectPaymentMethodParams(
    val paymentMethodType: String,
    val cardNetwork: String? = null
) : BaseActionUpdateParams
