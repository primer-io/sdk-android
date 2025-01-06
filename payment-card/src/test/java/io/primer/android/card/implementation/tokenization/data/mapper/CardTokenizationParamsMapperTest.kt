package io.primer.android.card.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.tokenization.data.model.CardPaymentInstrumentDataRequest
import io.primer.android.card.implementation.tokenization.domain.model.CardPaymentInstrumentParams
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.payments.core.tokenization.data.model.TokenizationCheckoutRequestV2
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardTokenizationParamsMapperTest {
    private lateinit var mapper: CardTokenizationParamsMapper

    @BeforeEach
    fun setUp() {
        mapper = CardTokenizationParamsMapper()
    }

    @Test
    fun `map should correctly map CardPaymentInstrumentParams to CardPaymentInstrumentDataRequest`() {
        // Arrange
        val cardParams =
            CardPaymentInstrumentParams(
                paymentMethodType = "credit_card",
                number = "4111111111111111",
                expirationMonth = "12",
                expirationYear = "2024",
                cvv = "123",
                cardholderName = "John Doe",
                preferredNetwork = CardNetwork.Type.VISA,
            )
        val tokenizationParams =
            TokenizationParams(
                paymentInstrumentParams = cardParams,
                sessionIntent = PrimerSessionIntent.CHECKOUT,
            )

        // Act
        val result = mapper.map(tokenizationParams)

        // Assert
        val paymentInstrument =
            CardPaymentInstrumentDataRequest(
                number = cardParams.number,
                expirationMonth = cardParams.expirationMonth,
                expirationYear = cardParams.expirationYear,
                cvv = cardParams.cvv,
                cardholderName = cardParams.cardholderName,
                preferredNetwork = cardParams.preferredNetwork,
            )

        val expectedTokenizationRequest =
            TokenizationCheckoutRequestV2(
                paymentInstrument = paymentInstrument,
                paymentInstrumentSerializer = CardPaymentInstrumentDataRequest.serializer,
            )

        assertEquals(expectedTokenizationRequest, result)
    }
}
