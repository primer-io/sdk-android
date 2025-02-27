package io.primer.android.clientSessionActions.domain.models

data class ActionUpdateBillingAddressParams(
    val firstName: String? = null,
    val lastName: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val countryCode: String? = null,
    val state: String? = null,
) : BaseActionUpdateParams
