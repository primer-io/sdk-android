package io.primer.bancontact.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.implementation.tokenization.data.mapper.AdyenBancontactTokenizationParamsMapper
import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactPaymentInstrumentDataRequest
import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactSessionInfoDataRequest
import io.primer.android.bancontact.implementation.tokenization.domain.model.AdyenBancontactPaymentInstrumentParams
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AdyenBancontactTokenizationParamsMapperTest {

    private val mapper = AdyenBancontactTokenizationParamsMapper()

    @Test
    fun `map should correctly map TokenizationParams to TokenizationRequestV2`() {
        // Given
        val paymentMethodType = "AydenBancontact"
        val paymentInstrumentParams = AdyenBancontactPaymentInstrumentParams(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = "testPaymentMethodConfigId",
            locale = "testLocale",
            redirectionUrl = "testRedirectionUrl",
            number = "testNumber",
            expirationMonth = "testExpirationMonth",
            expirationYear = "testExpirationYear",
            cardholderName = "testCardholderName",
            userAgent = "testUserAgent"
        )
        val sessionIntent = PrimerSessionIntent.CHECKOUT
        val tokenizationParams = TokenizationParams(paymentInstrumentParams, sessionIntent)

        // When
        val result = mapper.map(tokenizationParams)

        val instrumentRequest = AdyenBancontactPaymentInstrumentDataRequest(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = "testPaymentMethodConfigId",
            sessionInfo = AdyenBancontactSessionInfoDataRequest(
                redirectionUrl = "testRedirectionUrl",
                locale = "testLocale",
                userAgent = "testUserAgent"
            ),
            type = PaymentInstrumentType.CARD_OFF_SESSION_PAYMENT,
            number = "testNumber",
            expirationMonth = "testExpirationMonth",
            expirationYear = "testExpirationYear",
            cardholderName = "testCardholderName",
            authenticationProvider = null
        )

        // Then
        val expectedRequest = instrumentRequest.toTokenizationRequest(sessionIntent)

        assertEquals(expectedRequest, result)
    }
}
