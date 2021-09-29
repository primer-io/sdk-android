package io.primer.android.model.dto

import io.primer.android.model.OrderItem
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    var id: String? = null,
    var amount: Int? = null,
    var currency: String? = null,
    var countryCode: CountryCode? = null,
    var description: String? = null,
    var items: List<OrderItem> = emptyList(),
)
