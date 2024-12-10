package io.primer.sample.datamodels

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
            vaultOnSuccess: Boolean? = null,
            vaultOnAgreement: Boolean? = null
        ): TransactionRequest {
            return TransactionRequest(
                paymentMethodToken = paymentMethod,
                paymentMethod = PaymentMethodPayload(
                    descriptor = descriptor,
                    paymentType = paymentType,
                    vaultOnSuccess = vaultOnSuccess,
                    vaultOnAgreement = vaultOnAgreement
                )
            )
        }
    }
}
