// package structure is kept in order to maintain backward compatibility
package io.primer.android.domain.payments.create.model

import io.primer.android.payments.core.create.data.model.PaymentStatus

data class Payment(
    val id: String,
    val orderId: String,
    val status: PaymentStatus? = null,
) {
    companion object {
        val undefined by lazy { Payment(id = "undefined", orderId = "undefined") }
    }
}
