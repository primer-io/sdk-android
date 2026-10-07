package io.primer.android.stripe.model

import java.io.Serializable

data class StripeAchBank(
    val id: String,
    val name: String,
    val last4: String
) : Serializable
