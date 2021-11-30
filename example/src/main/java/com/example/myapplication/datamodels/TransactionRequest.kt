package com.example.myapplication.datamodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.primer.android.model.dto.PrimerConfig

@Keep
data class TransactionRequest(
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("environment") val environment: String,
    @SerializedName("isV3") val isV3: Boolean = true,
    ) : ExampleAppRequestBody {
    companion object {
        fun create(
            paymentMethod: String,
            environment: String,
        ): TransactionRequest {
            return TransactionRequest(
                paymentMethod,
                1000,
                "dirk",
                "SEK",
                "SE",
                environment,
            )
        }
    }
}
