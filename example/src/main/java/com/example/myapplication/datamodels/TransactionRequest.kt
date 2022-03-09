package com.example.myapplication.datamodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TransactionRequest(
    @SerializedName("paymentMethodToken") val paymentMethodToken: String,
    @SerializedName("paymentMethod") val paymentMethod: PaymentMethodPayload
) : ExampleAppRequestBody {
    companion object {
        fun create(
            paymentMethod: String,
            descriptor: String,
            paymentType: PaymentMethodPayload.PaymentType = PaymentMethodPayload.PaymentType.FIRST_PAYMENT,
            vaultOnSuccess: Boolean = true
        ): TransactionRequest {
            return TransactionRequest(
                paymentMethod,
                PaymentMethodPayload(
                    descriptor,
                    paymentType,
                    vaultOnSuccess
                )
            )
        }
    }
}
