package io.primer.android.clientSessionActions.domain.models

data class ActionUpdateCustomerDetailsParams(
    val firstName: String?,
    val lastName: String?,
    val emailAddress: String?,
) : BaseActionUpdateParams
