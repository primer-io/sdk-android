package io.primer.android.webredirect.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectPaymentInstrumentDataRequest
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectSessionInfoDataRequest
import io.primer.android.webredirect.implementation.tokenization.domain.model.WebRedirectPaymentInstrumentParams
import org.junit.jupiter.api.Test

class WebRedirectTokenizationParamsMapperTest {
    private val mapper = WebRedirectTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "NOL_PAY"
        val paymentInstrumentParams =
            WebRedirectPaymentInstrumentParams(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                locale = "testLocale",
                redirectionUrl = "testRedirectionUrl",
                platform = "ANDROID",
            )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest =
            WebRedirectPaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = "testPaymentMethodConfigId",
                sessionInfo =
                WebRedirectSessionInfoDataRequest(
                    redirectionUrl = "testRedirectionUrl",
                    locale = "testLocale",
                ),
                type = PaymentInstrumentType.OFF_SESSION_PAYMENT,
            )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assert(result == expectedRequest)
    }
}
