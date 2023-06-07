package io.primer.android.data.tokenization.models.paymentInstruments.paypal

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalShippingAddressDataResponse
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalExternalPayerInfo
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal data class PaypalVaultPaymentInstrumentDataRequest(
    val billingAgreementId: String?,
    val externalPayerInfo: PaypalExternalPayerInfo,
    val shippingAddressDataResponse: PaypalShippingAddressDataResponse?
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val BILLING_AGREEMENT_ID_FIELD = "paypalBillingAgreementId"
        private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"
        private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<PaypalVaultPaymentInstrumentDataRequest> {
                override fun serialize(t: PaypalVaultPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        putOpt(BILLING_AGREEMENT_ID_FIELD, t.billingAgreementId)
                        put(
                            EXTERNAL_PAYER_INFO_FIELD,
                            t.externalPayerInfo.let {
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<PaypalExternalPayerInfo>()
                                    .serialize(it)
                            }
                        )
                        putOpt(
                            SHIPPING_ADDRESS_FIELD,
                            t.shippingAddressDataResponse?.let {
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<PaypalShippingAddressDataResponse>()
                                    .serialize(it)
                            }
                        )
                    }
                }
            }
    }
}
