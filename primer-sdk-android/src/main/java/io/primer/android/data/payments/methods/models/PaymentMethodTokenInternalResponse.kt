package io.primer.android.data.payments.methods.models

import io.primer.android.model.dto.PaymentMethodTokenInternal
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentMethodTokenInternalResponse(val data: List<PaymentMethodTokenInternal>)
