package io.primer.android.klarna.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest

sealed interface KlarnaPaymentInstrumentDataRequest : BasePaymentInstrumentDataRequest {

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<KlarnaPaymentInstrumentDataRequest> {
            when (it) {
                is KlarnaCheckoutPaymentInstrumentDataRequest ->
                    KlarnaCheckoutPaymentInstrumentDataRequest.serializer.serialize(it)

                is KlarnaVaultPaymentInstrumentDataRequest ->
                    KlarnaVaultPaymentInstrumentDataRequest.serializer.serialize(it)
            }
        }
    }
}
