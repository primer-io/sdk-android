package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrimerSettings(
    @Deprecated(
        "Passing order in the settings is now deprecated. Please pass this data " +
            "when creating a client session with POST /client-session."
    ) var order: Order = Order(),
    var business: Business = Business(),
    @Deprecated(
        "Passing customer in the settings is now deprecated. Please pass this data" +
            " when creating a client session with POST /client-session."
    ) var customer: Customer = Customer(),
    var options: Options = Options(),
)
