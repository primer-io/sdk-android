package io.primer.android.threeds.data.models

import kotlinx.serialization.Serializable

@Serializable
internal data class Customer(
    val name: String? = null,
    val email: String? = null,
    val homePhone: String? = null,
    val mobilePhone: String? = null,
    val workPhone: String? = null,
)
