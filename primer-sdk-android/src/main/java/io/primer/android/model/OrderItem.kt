package io.primer.android.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val name: String,
    val unitAmount: Int? = null,
    val quantity: Int,
    val isPending: Boolean = false,
)
