package io.primer.sample.datamodels

import com.google.gson.annotations.SerializedName

data class PaymentMethodPayload(
    @SerializedName("descriptor") val descriptor: String,
    @SerializedName("paymentType") val paymentType: PaymentType = PaymentType.FIRST_PAYMENT,
    @SerializedName("vaultOnSuccess") val vaultOnSuccess: Boolean?,
    @SerializedName("vaultOnAgreement") val vaultOnAgreement: Boolean?
) {
    enum class PaymentType {
        FIRST_PAYMENT, ECOMMERCE, SUBSCRIPTION, UNSCHEDULED
    }
}
