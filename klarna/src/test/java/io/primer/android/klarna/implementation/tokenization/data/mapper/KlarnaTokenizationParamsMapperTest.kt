package io.primer.android.klarna.implementation.tokenization.data.mapper

import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionData
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaCheckoutPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaVaultPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaPaymentInstrumentParams
import io.primer.android.klarna.implementation.tokenization.domain.model.KlarnaVaultPaymentInstrumentParams
import io.primer.android.payments.core.tokenization.data.model.TokenizationCheckoutRequestV2
import io.primer.android.payments.core.tokenization.data.model.TokenizationVaultRequestV2
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KlarnaTokenizationParamsMapperTest {
    private lateinit var mapper: KlarnaTokenizationParamsMapper

    @BeforeEach
    fun setUp() {
        mapper = KlarnaTokenizationParamsMapper()
    }

    @Test
    fun `map should return TokenizationRequestV2 for KlarnaCheckoutPaymentInstrumentParams`() {
        // Arrange
        val klarnaAuthorizationToken = "klarnaToken"
        val klarnaSessionData = mockk<KlarnaSessionData>(relaxed = true)

        val paymentInstrumentParams =
            KlarnaCheckoutPaymentInstrumentParams(
                klarnaAuthorizationToken = klarnaAuthorizationToken,
                sessionData = klarnaSessionData,
            )
        val tokenizationParams =
            TokenizationParams<KlarnaPaymentInstrumentParams>(
                paymentInstrumentParams = paymentInstrumentParams,
                sessionIntent = PrimerSessionIntent.CHECKOUT,
            )

        // Act
        val result = mapper.map(tokenizationParams)

        // Assert
        val expectedRequest =
            KlarnaCheckoutPaymentInstrumentDataRequest(
                klarnaAuthorizationToken = klarnaAuthorizationToken,
                sessionData = klarnaSessionData,
            )
        val expectedTokenizationRequest =
            TokenizationCheckoutRequestV2(
                paymentInstrument = expectedRequest,
                paymentInstrumentSerializer = KlarnaPaymentInstrumentDataRequest.serializer,
            )
        assertEquals(expectedTokenizationRequest, result)
    }

    @Test
    fun `map should return TokenizationRequestV2 for KlarnaVaultPaymentInstrumentParams`() {
        // Arrange
        val klarnaAuthorizationToken = "klarnaToken"
        val klarnaSessionData = mockk<KlarnaSessionData>(relaxed = true)

        val paymentInstrumentParams =
            KlarnaVaultPaymentInstrumentParams(
                klarnaCustomerToken = klarnaAuthorizationToken,
                sessionData = klarnaSessionData,
            )
        val tokenizationParams =
            TokenizationParams<KlarnaPaymentInstrumentParams>(
                paymentInstrumentParams = paymentInstrumentParams,
                sessionIntent = PrimerSessionIntent.VAULT,
            )

        // Act
        val result = mapper.map(tokenizationParams)

        // Assert
        val expectedRequest =
            KlarnaVaultPaymentInstrumentDataRequest(
                klarnaCustomerToken = klarnaAuthorizationToken,
                sessionData = klarnaSessionData,
            )
        val expectedTokenizationRequest =
            TokenizationVaultRequestV2(
                paymentInstrument = expectedRequest,
                paymentInstrumentSerializer = KlarnaPaymentInstrumentDataRequest.serializer,
                tokenType = "MULTI_USE",
                paymentFlow = "VAULT",
            )
        assertEquals(expectedTokenizationRequest, result)
    }
}
