package io.primer.android.model.dto

import io.primer.android.domain.action.models.PrimerAddress
import kotlinx.serialization.Serializable

@Serializable
data class PrimerBusiness(
    val name: String? = null,
    val registrationNumber: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: PrimerAddress? = null,
)
