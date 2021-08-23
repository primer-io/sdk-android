package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CustomerAccount(
    val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val passwordUpdatedAt: String?,
    val purchaseCount: Int?,
)
