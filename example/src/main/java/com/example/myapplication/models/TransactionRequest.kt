package com.example.myapplication.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TransactionRequest(
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("capture") val capture: Boolean,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("type") val type: String,
) : ExampleAppRequestBody
