package io.primer.android.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrimerSettings(
    var order: Order = Order(),
    var business: Business = Business(),
    var customer: Customer = Customer(),
    var options: Options = Options(),
)
