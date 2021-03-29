package io.primer.android.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val name: String,
    val unitAmount: Int,
    val quantity: Int? = null,
    val reference: String? = null,
)
