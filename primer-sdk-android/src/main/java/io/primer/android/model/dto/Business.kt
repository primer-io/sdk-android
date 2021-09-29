package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class Business(
    val name: String? = null,
    val registrationNumber: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: Address? = null,
)
