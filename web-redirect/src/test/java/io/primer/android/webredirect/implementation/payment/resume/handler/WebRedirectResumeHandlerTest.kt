package io.primer.android.webredirect.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository
import io.primer.android.webredirect.implementation.payment.resume.clientToken.data.WebRedirectPaymentMethodClientTokenParser
import io.primer.android.webredirect.implementation.payment.resume.clientToken.domain.model.WebRedirectClientToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WebRedirectResumeHandlerTest {
    private val clientTokenParser = mockk<WebRedirectPaymentMethodClientTokenParser>()
    private val tokenizedPaymentMethodRepository = mockk<TokenizedPaymentMethodRepository>()
    private val configurationRepository = mockk<ConfigurationRepository>()
    private val deeplinkRepository = mockk<RedirectDeeplinkRepository>()
    private val validateClientTokenRepository = mockk<ValidateClientTokenRepository>()
    private val clientTokenRepository = mockk<ClientTokenRepository>()
    private val checkoutAdditionalInfoHandler = mockk<CheckoutAdditionalInfoHandler>()
    private val redirectUrl = "testRedirectUrl"
    private val statusUrl = "testStatusUrl"
    private val clientTokenIntent = "testIntent"
    private val testPaymentMethodType = "ADYEN_GIROPAY"

    private val handler =
        WebRedirectResumeHandler(
            clientTokenParser = clientTokenParser,
            tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
            configurationRepository = configurationRepository,
            deeplinkRepository = deeplinkRepository,
            validateClientTokenRepository = validateClientTokenRepository,
            clientTokenRepository = clientTokenRepository,
            checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
        )

    @BeforeEach
    fun setUp() {
        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns
            mockk {
                every { paymentMethodType } returns testPaymentMethodType
            }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getResumeDecision should return the correct WebRedirectDecision when called with a WebRedirectClientToken`() {
        // Given
        every { configurationRepository.getConfiguration() } returns
            mockk {
                every { paymentMethods } returns
                    listOf(
                        mockk {
                            every { type } returns testPaymentMethodType
                            every { name } returns ""
                        },
                    )
            }

        every { deeplinkRepository.getDeeplinkUrl() } returns ""

        val clientToken = WebRedirectClientToken(redirectUrl, statusUrl, clientTokenIntent)
        every { clientTokenParser.parseClientToken(any()) } returns clientToken

        runTest {
            // When
            val decision = handler.getResumeDecision(clientToken)

            // Then
            val expected =
                WebRedirectDecision(
                    title = "",
                    paymentMethodType = testPaymentMethodType,
                    redirectUrl = redirectUrl,
                    statusUrl = statusUrl,
                    deeplinkUrl = "",
                )
            assertEquals(expected, decision)
        }

        verify { tokenizedPaymentMethodRepository.getPaymentMethod() }
        verify { configurationRepository.getConfiguration() }
    }

    @Test
    fun `supportedClientTokenIntents should return a single item list with ADYEN_GIROPAY`() {
        // When
        val intents = handler.supportedClientTokenIntents()

        // Then
        val expected = listOf("${testPaymentMethodType}_REDIRECTION")
        assertEquals(expected, intents)
    }
}
