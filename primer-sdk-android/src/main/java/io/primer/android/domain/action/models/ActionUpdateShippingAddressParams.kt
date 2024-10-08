package io.primer.android.domain.action.models

internal data class ActionUpdateShippingAddressParams(
    val firstName: String? = null,
    val lastName: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val countryCode: String? = null,
    val state: String? = null
) : BaseActionUpdateParams
