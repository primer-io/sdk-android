package io.primer.android.card.implementation.tokenization.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.tokenization.domain.CardTokenizationInteractor
import io.primer.android.card.implementation.tokenization.domain.model.CardPaymentInstrumentParams
import io.primer.android.card.implementation.tokenization.presentation.composable.CardTokenizationInputable
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardTokenizationDelegateTest {
    @MockK
    private lateinit var tokenizationInteractor: CardTokenizationInteractor

    private lateinit var cardTokenizationDelegate: CardTokenizationDelegate

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        cardTokenizationDelegate = CardTokenizationDelegate(tokenizationInteractor)
    }

    @Test
    fun `mapTokenizationData should map input correctly`() =
        runTest {
            val input =
                CardTokenizationInputable(
                    paymentMethodType = "credit_card",
                    cardData =
                    PrimerCardData(
                        cardNumber = "4111111111111111",
                        expiryDate = "12/25",
                        cvv = "123",
                        cardHolderName = "John Doe",
                        cardNetwork = CardNetwork.Type.VISA,
                    ),
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val expectedParams =
                TokenizationParams(
                    paymentInstrumentParams =
                    CardPaymentInstrumentParams(
                        paymentMethodType = "credit_card",
                        number = "4111111111111111",
                        expirationMonth = "12",
                        expirationYear = "25",
                        cvv = "123",
                        cardholderName = "John Doe",
                        preferredNetwork = CardNetwork.Type.VISA,
                    ),
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val result = cardTokenizationDelegate.mapTokenizationData(input).getOrNull()

            assertEquals(expectedParams, result)
        }

    @Test
    fun `mapTokenizationData should pad single digit expiration month`() =
        runTest {
            val input =
                CardTokenizationInputable(
                    paymentMethodType = "credit_card",
                    cardData =
                    PrimerCardData(
                        cardNumber = "4111111111111111",
                        expiryDate = "5/25",
                        cvv = "123",
                        cardHolderName = "John Doe",
                        cardNetwork = CardNetwork.Type.VISA,
                    ),
                    primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val expectedParams =
                TokenizationParams(
                    paymentInstrumentParams =
                    CardPaymentInstrumentParams(
                        paymentMethodType = "credit_card",
                        number = "4111111111111111",
                        expirationMonth = "05",
                        expirationYear = "25",
                        cvv = "123",
                        cardholderName = "John Doe",
                        preferredNetwork = CardNetwork.Type.VISA,
                    ),
                    sessionIntent = PrimerSessionIntent.CHECKOUT,
                )

            val result = cardTokenizationDelegate.mapTokenizationData(input).getOrNull()

            assertEquals(expectedParams, result)
        }
}
