package io.primer.android.model.dto

import io.primer.android.model.LineItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    @SerialName("orderId") var id: String? = null,
    @SerialName("currencyCode") var currency: String? = null,
    @SerialName("merchantAmount") var amount: Int? = null,
    val totalOrderAmount: Int? = null,
    var countryCode: CountryCode? = null,
    var description: String? = null,
    @SerialName("lineItems") var items: List<LineItem> = emptyList(),
    val fees: List<Fee> = listOf(),
) {

    @Serializable
    data class Fee(
        val type: String,
        val amount: Int,
    )
}
