package io.primer.android.domain.action.models

internal data class ActionUpdateCustomerDetailsParams(
    val firstName: String?,
    val lastName: String?,
    val emailAddress: String?
) : BaseActionUpdateParams
