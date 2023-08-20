package io.primer.android.data.tokenization.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.AsyncPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard.AdyenBancontactCardPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.card.CardPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.googlepay.GooglePayPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.klarna.KlarnaPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.PaypalCheckoutPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.PaypalVaultPaymentInstrumentDataRequest
import org.json.JSONObject

internal open class PaymentInstrumentDataRequest : JSONObjectSerializable {

    companion object {

        @JvmField
        val serializer = object : JSONObjectSerializer<PaymentInstrumentDataRequest> {
            override fun serialize(t: PaymentInstrumentDataRequest): JSONObject {
                return when (t) {
                    is KlarnaPaymentInstrumentDataRequest ->
                        KlarnaPaymentInstrumentDataRequest.serializer.serialize(t)
                    is AdyenBancontactCardPaymentInstrumentDataRequest ->
                        AdyenBancontactCardPaymentInstrumentDataRequest.serializer.serialize(
                            t
                        )
                    is AsyncPaymentInstrumentDataRequest ->
                        AsyncPaymentInstrumentDataRequest.serializer.serialize(
                            t
                        )
                    is PaypalCheckoutPaymentInstrumentDataRequest ->
                        PaypalCheckoutPaymentInstrumentDataRequest.serializer.serialize(
                            t
                        )
                    is PaypalVaultPaymentInstrumentDataRequest ->
                        PaypalVaultPaymentInstrumentDataRequest.serializer.serialize(
                            t
                        )
                    is GooglePayPaymentInstrumentDataRequest ->
                        GooglePayPaymentInstrumentDataRequest.serializer.serialize(
                            t
                        )
                    is CardPaymentInstrumentDataRequest ->
                        CardPaymentInstrumentDataRequest.serializer.serialize(t)
                    is NolPayPaymentInstrumentDataRequest ->
                        NolPayPaymentInstrumentDataRequest.serializer.serialize(t)
                    else -> error("Missing serializer mapping for $t")
                }
            }
        }
    }
}
