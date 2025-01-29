package io.primer.android.qrcode.implementation.tokenization.data.mapper

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodePaymentInstrumentDataRequest
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodeSessionInfoDataRequest
import io.primer.android.qrcode.implementation.tokenization.domain.model.QrCodePaymentInstrumentParams

internal class QrCodeTokenizationParamsMapper :
    TokenizationParamsMapper<QrCodePaymentInstrumentParams, QrCodePaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<QrCodePaymentInstrumentParams>,
    ): TokenizationRequestV2<QrCodePaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            QrCodePaymentInstrumentDataRequest(
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                QrCodeSessionInfoDataRequest(
                    locale = paymentInstrumentParams.locale,
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
