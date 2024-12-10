// package structure is kept in order to maintain backward compatibility
package io.primer.android.domain.payments.create.model

data class Payment(
    val id: String,
    val orderId: String
) {
    companion object {
        val undefined by lazy { Payment(id = "undefined", orderId = "undefined") }
    }
}
