package com.example.myapplication.datamodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.primer.android.model.dto.PrimerConfig

@Keep
data class TransactionRequest(
    @SerializedName("paymentMethodToken") val paymentMethod: String,
) : ExampleAppRequestBody {
    companion object {
        fun create(
            paymentMethod: String,
        ): TransactionRequest {
            return TransactionRequest(
                paymentMethod,
            )
        }
    }
}
