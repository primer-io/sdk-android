package io.primer.android.data.payments.create.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CreatePaymentRequest(private val paymentMethodToken: String)
