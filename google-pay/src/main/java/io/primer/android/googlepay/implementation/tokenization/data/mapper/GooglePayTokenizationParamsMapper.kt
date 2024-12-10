package io.primer.android.googlepay.implementation.tokenization.data.mapper

import android.util.Base64
import io.primer.android.googlepay.implementation.tokenization.data.model.GooglePayPaymentInstrumentDataRequest
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.jetbrains.annotations.VisibleForTesting
import org.json.JSONObject

internal class GooglePayTokenizationParamsMapper :
    TokenizationParamsMapper<GooglePayPaymentInstrumentParams, GooglePayPaymentInstrumentDataRequest> {
    override fun map(params: TokenizationParams<GooglePayPaymentInstrumentParams>):
        TokenizationRequestV2<GooglePayPaymentInstrumentDataRequest> {
        val token = JSONObject(params.paymentInstrumentParams.paymentData.toJson())
            .getJSONObject(PAYMENT_METHOD_DATA_FIELD)
            .getJSONObject(TOKENIZATION_DATA_FIELD)
            .getString(TOKENIZATION_FIELD)
        val instrumentDataRequest = GooglePayPaymentInstrumentDataRequest(
            params.paymentInstrumentParams.merchantId,
            Base64.encodeToString(token.toByteArray(), Base64.NO_WRAP),
            params.paymentInstrumentParams.flow
        )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }

    @VisibleForTesting
    internal companion object {

        const val PAYMENT_METHOD_DATA_FIELD = "paymentMethodData"
        const val TOKENIZATION_DATA_FIELD = "tokenizationData"
        const val TOKENIZATION_FIELD = "token"
    }
}
