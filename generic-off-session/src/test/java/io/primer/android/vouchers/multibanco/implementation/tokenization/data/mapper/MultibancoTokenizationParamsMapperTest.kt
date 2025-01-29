package io.primer.android.vouchers.multibanco.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoPaymentInstrumentDataRequest
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoSessionInfoDataRequest
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.model.MultibancoPaymentInstrumentParams
import org.junit.jupiter.api.Test

internal class MultibancoTokenizationParamsMapperTest {
    private val mapper = MultibancoTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "multibanco"
        val paymentInstrumentParams =
            MultibancoPaymentInstrumentParams(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                locale = "testLocale",
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            MultibancoPaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                sessionInfo =
                MultibancoSessionInfoDataRequest(
                    locale = "testLocale",
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
