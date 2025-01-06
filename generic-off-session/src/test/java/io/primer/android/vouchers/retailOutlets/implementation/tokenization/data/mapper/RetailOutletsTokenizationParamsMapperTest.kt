package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsPaymentInstrumentDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsSessionInfoDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.model.RetailOutletsPaymentInstrumentParams
import org.junit.jupiter.api.Test

internal class RetailOutletsTokenizationParamsMapperTest {
    private val mapper = RetailOutletsTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "multibanco"
        val paymentInstrumentParams =
            RetailOutletsPaymentInstrumentParams(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                locale = "testLocale",
                retailOutlet = "testRetailOutlet",
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            RetailOutletsPaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                sessionInfo =
                    RetailOutletsSessionInfoDataRequest(
                        locale = "testLocale",
                        retailerOutlet = "testRetailOutlet",
                    ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
