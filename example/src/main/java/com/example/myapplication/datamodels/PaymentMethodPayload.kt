package com.example.myapplication.datamodels

import com.google.gson.annotations.SerializedName

data class PaymentMethodPayload(
    @SerializedName("descriptor") val descriptor: String,
    @SerializedName("paymentType") val paymentType: PaymentType = PaymentType.ECOMMERCE,
    @SerializedName("vaultOnSuccess") val vaultOnSuccess: Boolean = true
) {
    enum class PaymentType {
        FIRST_PAYMENT, ECOMMERCE, SUBSCRIPTION, UNSCHEDULED
    }
}
