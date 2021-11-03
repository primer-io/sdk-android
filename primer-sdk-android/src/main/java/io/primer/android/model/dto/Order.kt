package io.primer.android.model.dto

import io.primer.android.model.OrderItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    @SerialName("orderId") var id: String? = null,
    @SerialName("totalAmount") var amount: Int? = null,
    @SerialName("currencyCode") var currency: String? = null,
    var countryCode: CountryCode? = null,
    var description: String? = null,
    var items: List<OrderItem> = emptyList(),
)
