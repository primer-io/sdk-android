package io.primer.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias LineItem = OrderItem

@Serializable
data class OrderItem(
    val name: String,
    val itemId: String,
    val description: String,
    @SerialName("amount") val unitAmount: Int,
    val quantity: Int,
    val discountAmount: Int = 0,
) {
    val calculatedAmount: Int get() = (unitAmount * quantity) - discountAmount
}
