package io.primer.android.clientSessionActions.domain.models

data class ActionUpdateSelectPaymentMethodParams(
    val paymentMethodType: String,
    val cardNetwork: String? = null,
) : BaseActionUpdateParams
