package io.primer.android.sandboxProcessor.implementation.tokenization.data.mapper

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.sandboxProcessor.implementation.tokenization.data.model.SandboxProcessorPaymentInstrumentDataRequest
import io.primer.android.sandboxProcessor.implementation.tokenization.data.model.SandboxProcessorSessionInfoDataRequest
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.model.SandboxProcessorPaymentInstrumentParams

internal class SandboxProcessorTokenizationParamsMapper :
    TokenizationParamsMapper<SandboxProcessorPaymentInstrumentParams, SandboxProcessorPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<SandboxProcessorPaymentInstrumentParams>,
    ): TokenizationRequestV2<SandboxProcessorPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            SandboxProcessorPaymentInstrumentDataRequest(
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                    SandboxProcessorSessionInfoDataRequest(
                        flowDecision = paymentInstrumentParams.flowDecision,
                    ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
