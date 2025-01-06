package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import org.json.JSONObject

internal sealed interface PaypalPaymentInstrumentDataRequest : BasePaymentInstrumentDataRequest {
    data class PaypalCheckoutPaymentInstrumentDataRequest(
        val paypalOrderId: String?,
        val externalPayerInfo: ExternalPayerInfoRequest?,
    ) : PaypalPaymentInstrumentDataRequest {
        companion object {
            private const val PAYPAL_ORDER_ID_FIELD = "paypalOrderId"
            private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"

            @JvmField
            val serializer =
                JSONObjectSerializer<PaypalCheckoutPaymentInstrumentDataRequest> { t ->
                    JSONObject().apply {
                        putOpt(PAYPAL_ORDER_ID_FIELD, t.paypalOrderId)
                        put(
                            EXTERNAL_PAYER_INFO_FIELD,
                            t.externalPayerInfo?.let {
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<ExternalPayerInfoRequest>()
                                    .serialize(it)
                            },
                        )
                    }
                }
        }
    }

    data class PaypalVaultPaymentInstrumentDataRequest(
        val billingAgreementId: String?,
        val externalPayerInfo: PaypalExternalPayerInfo,
        val shippingAddress: PaypalShippingAddressDataResponse?,
    ) : PaypalPaymentInstrumentDataRequest {
        companion object {
            private const val BILLING_AGREEMENT_ID_FIELD = "paypalBillingAgreementId"
            private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"
            private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"

            @JvmField
            val serializer =
                JSONObjectSerializer<PaypalVaultPaymentInstrumentDataRequest> { t ->
                    JSONObject().apply {
                        putOpt(BILLING_AGREEMENT_ID_FIELD, t.billingAgreementId)
                        put(
                            EXTERNAL_PAYER_INFO_FIELD,
                            t.externalPayerInfo.let {
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<PaypalExternalPayerInfo>()
                                    .serialize(it)
                            },
                        )
                        putOpt(
                            SHIPPING_ADDRESS_FIELD,
                            t.shippingAddress?.let {
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<PaypalShippingAddressDataResponse>()
                                    .serialize(it)
                            },
                        )
                    }
                }
        }
    }

    companion object {
        @JvmField
        val serializer =
            JSONObjectSerializer<PaypalPaymentInstrumentDataRequest> { t ->
                when (t) {
                    is PaypalCheckoutPaymentInstrumentDataRequest ->
                        PaypalCheckoutPaymentInstrumentDataRequest.serializer.serialize(t)

                    is PaypalVaultPaymentInstrumentDataRequest ->
                        PaypalVaultPaymentInstrumentDataRequest.serializer.serialize(t)
                }
            }
    }
}
