package io.primer.android.stripe.model

import java.io.Serializable

data class StripeAchBank(
    val id: String,
    val name: String,
    val last4: String,
) : Serializable {
    private companion object {
        private const val serialVersionUID: Long = 1L
    }
}
