package io.primer.android.data.configuration.models

import io.primer.android.domain.action.models.PrimerLineItem
import io.primer.android.domain.action.models.PrimerOrder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OrderDataResponse(
    @SerialName("orderId") var id: String? = null,
    @SerialName("currencyCode") var currency: String? = null,
    @SerialName("merchantAmount") var amount: Int? = null,
    val totalOrderAmount: Int? = null,
    var countryCode: CountryCode? = null,
    var description: String? = null,
    var lineItems: List<LineItemDataResponse> = emptyList(),
    val fees: List<Fee> = listOf(),
) {
    @Serializable
    data class LineItemDataResponse(
        val name: String? = null,
        val itemId: String,
        val description: String,
        @SerialName("amount") val unitAmount: Int,
        val quantity: Int,
        val discountAmount: Int = 0,
        val taxAmount: Int? = null,
        val taxCode: String? = null,
    ) {
        fun toLineItem() = PrimerLineItem(
            itemId,
            description,
            unitAmount,
            discountAmount,
            quantity,
            taxCode,
            taxAmount
        )
    }

    @Serializable
    data class Fee(
        val type: String,
        val amount: Int,
    )

    fun toOrder() = PrimerOrder(countryCode)
}
