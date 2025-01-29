package io.primer.android.ipay88.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88PaymentInstrumentDataRequest
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88SessionInfoDataRequest
import io.primer.android.ipay88.implementation.tokenization.domain.model.IPay88PaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.Test

internal class IPay88TokenizationParamsMapperTest {
    private val mapper = IPay88TokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "IPAY88"
        val paymentInstrumentParams =
            IPay88PaymentInstrumentParams(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                locale = "testLocale",
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            IPay88PaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                sessionInfo =
                IPay88SessionInfoDataRequest(
                    locale = "testLocale",
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
