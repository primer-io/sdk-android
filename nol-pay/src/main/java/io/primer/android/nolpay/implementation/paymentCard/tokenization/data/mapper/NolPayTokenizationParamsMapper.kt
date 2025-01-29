package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.mapper

import android.os.Build
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPayPaymentInstrumentDataRequest
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPaySessionInfoDataRequest
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model.NolPayPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams

internal class NolPayTokenizationParamsMapper :
    TokenizationParamsMapper<NolPayPaymentInstrumentParams, NolPayPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<NolPayPaymentInstrumentParams>,
    ): TokenizationRequestV2<NolPayPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            NolPayPaymentInstrumentDataRequest(
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                NolPaySessionInfoDataRequest(
                    mobileCountryCode = paymentInstrumentParams.mobileCountryCode,
                    mobileNumber = paymentInstrumentParams.mobileNumber,
                    nolPayCardNumber = paymentInstrumentParams.nolPayCardNumber,
                    deviceVendor = Build.MANUFACTURER,
                    deviceModel = Build.MODEL,
                ),
                type = params.paymentInstrumentParams.type,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
