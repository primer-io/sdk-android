package io.primer.android.googlepay.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.GooglePayClientTokenParser
import io.primer.android.googlepay.implementation.payment.resume.clientToken.domain.model.GooglePayClientToken
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.processor3ds.domain.model.Processor3DS
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GooglePayResumeHandlerTest {
    private val clientTokenParser = mockk<GooglePayClientTokenParser>()
    private val validateClientTokenRepository = mockk<ValidateClientTokenRepository>()
    private val clientTokenRepository = mockk<ClientTokenRepository>()
    private val checkoutAdditionalInfoHandler = mockk<CheckoutAdditionalInfoHandler>()
    private val tokenizedPaymentMethodRepository = mockk<TokenizedPaymentMethodRepository>()

    private val handler =
        GooglePayResumeHandler(
            clientTokenParser,
            validateClientTokenRepository,
            clientTokenRepository,
            checkoutAdditionalInfoHandler,
            tokenizedPaymentMethodRepository,
        )

    @BeforeEach
    fun setUp() {
        // No need to mock GooglePayNative3DSClientTokenParser since it's not an object
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getResumeDecision should return the correct GooglePayResumeDecision when called with a GooglePayNative3DSClientToken`() {
        // Given
        val clientToken =
            GooglePayClientToken.GooglePayNative3DSClientToken(
                clientTokenIntent = ClientTokenIntent.`3DS_AUTHENTICATION`.name,
                supportedThreeDsProtocolVersions = listOf("2.1.0", "2.2.0"),
            )
        every { clientTokenParser.parseClientToken(any()) } returns clientToken
        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns mockk {
            every { token } returns "token"
            every { paymentInstrumentData } returns mockk {
                every { binData } returns mockk {
                    every { network } returns "VISA"
                }
            }
        }

        runTest {
            // When
            val decision = handler.getResumeDecision(clientToken)

            // Then
            val expected = GooglePayResumeDecision.GooglePayNative3dsResumeDecision(
                supportedThreeDsProtocolVersions = listOf("2.1.0", "2.2.0"),
                paymentMethodToken = "token",
                cardNetwork = CardNetwork.Type.VISA,
            )
            assertEquals(expected, decision)
        }
    }

    @Test
    fun `getResumeDecision should return the correct GooglePayResumeDecision when called with a GooglePayProcessor3DSClientToken`() {
        // Given
        val clientToken =
            GooglePayClientToken.GooglePayProcessor3DSClientToken(
                clientTokenIntent = ClientTokenIntent.`3DS_AUTHENTICATION`.name,
                processor3DS =
                Processor3DS(
                    redirectUrl = "https://www.example/redirect",
                    statusUrl = "https://www.example/status",
                ),
            )
        every { clientTokenParser.parseClientToken(any()) } returns clientToken

        runTest {
            // When
            val decision = handler.getResumeDecision(clientToken)

            // Then
            val expected =
                GooglePayResumeDecision.GooglePayProcessor3dsResumeDecision(
                    Processor3DS(
                        redirectUrl = "https://www.example/redirect",
                        statusUrl = "https://www.example/status",
                    ),
                )
            assertEquals(expected, decision)
        }
    }

    @Test
    fun `supportedClientTokenIntents should return a single item list with 3DS_AUTHENTICATION`() {
        // When
        val intents = handler.supportedClientTokenIntents()

        // Then
        val expected = ClientTokenIntent.`3DS_AUTHENTICATION`.name
        assertContains(intents, expected)
    }

    @Test
    fun `supportedClientTokenIntents should return a single item list with PROCESSOR_3DS`() {
        // When
        val intents = handler.supportedClientTokenIntents()

        // Then
        val expected = ClientTokenIntent.PROCESSOR_3DS.name
        assertContains(intents, expected)
    }
}
