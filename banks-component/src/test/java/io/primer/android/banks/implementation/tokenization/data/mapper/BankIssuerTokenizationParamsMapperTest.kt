package io.primer.android.banks.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerPaymentInstrumentDataRequest
import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerSessionInfoDataRequest
import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.Test

class BankIssuerTokenizationParamsMapperTest {
    private val mapper = BankIssuerTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "BankIssuer"
        val paymentInstrumentParams =
            BankIssuerPaymentInstrumentParams(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                locale = "testLocale",
                redirectionUrl = "testRedirectionUrl",
                bankIssuer = "testBankIssuer",
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            BankIssuerPaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                sessionInfo =
                BankIssuerSessionInfoDataRequest(
                    redirectionUrl = "testRedirectionUrl",
                    locale = "testLocale",
                    issuer = "testBankIssuer",
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
