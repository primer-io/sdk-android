package io.primer.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val name: String,
    @SerialName("reference") val description: String,
    val unitAmount: Int,
    val quantity: Int,
)
