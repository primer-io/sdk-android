package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.mapper

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsPaymentInstrumentDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsSessionInfoDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.model.RetailOutletsPaymentInstrumentParams

internal class RetailOutletsTokenizationParamsMapper :
    TokenizationParamsMapper<RetailOutletsPaymentInstrumentParams, RetailOutletsPaymentInstrumentDataRequest> {
    override fun map(
        params: TokenizationParams<RetailOutletsPaymentInstrumentParams>,
    ): TokenizationRequestV2<RetailOutletsPaymentInstrumentDataRequest> {
        val paymentInstrumentParams = params.paymentInstrumentParams
        val instrumentDataRequest =
            RetailOutletsPaymentInstrumentDataRequest(
                paymentMethodType = paymentInstrumentParams.paymentMethodType,
                paymentMethodConfigId = paymentInstrumentParams.paymentMethodConfigId,
                sessionInfo =
                    RetailOutletsSessionInfoDataRequest(
                        retailerOutlet = paymentInstrumentParams.retailOutlet,
                        locale = paymentInstrumentParams.locale,
                    ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )
        return instrumentDataRequest.toTokenizationRequest(params.sessionIntent)
    }
}
