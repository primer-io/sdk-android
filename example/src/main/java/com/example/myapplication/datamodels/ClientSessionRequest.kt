package com.example.myapplication.datamodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.primer.android.model.OrderItem
import io.primer.android.model.dto.Address
import io.primer.android.model.dto.CountryCode

@Keep
data class ClientSessionRequest(
    @SerializedName("customerId") val id: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("customer") val customer: CustomerRequest? = null,
    @SerializedName("order") val orderRequest: OrderRequest? = null,
) : ExampleAppRequestBody

@Keep
data class CustomerRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val mobileNumber: String? = null,
    val emailAddress: String? = null,
    val billingAddress: AddressRequest? = null,
    val shippingAddress: AddressRequest? = null,
) : ExampleAppRequestBody

@Keep
data class AddressRequest(
    val addressLine1: String,
    val addressLine2: String? = null,
    val postalCode: String,
    val city: String,
    val countryCode: CountryCode,
)

fun Address.toAddressRequest() = AddressRequest(
    line1,
    line2,
    postalCode,
    city,
    countryCode
)

@Keep
data class OrderRequest(
    val countryCode: CountryCode,
    var lineItems: List<OrderItemRequest> = emptyList(),
) : ExampleAppRequestBody

@Keep
data class OrderItemRequest(
    val itemId: String,
    val description: String,
    val amount: Int,
    val quantity: Int,
) : ExampleAppRequestBody

fun OrderItem.toOrderItemRequest() = OrderItemRequest(
    name,
    description,
    unitAmount,
    quantity
)

interface ExampleAppRequestBody {}