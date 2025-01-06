package io.primer.bancontact.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.bancontact.implementation.payment.resume.clientToken.data.AdyenBancontactPaymentMethodClientTokenParser
import io.primer.android.bancontact.implementation.payment.resume.clientToken.domain.model.AdyenBancontactClientToken
import io.primer.android.bancontact.implementation.payment.resume.handler.AydenBancontactResumeHandler
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.PaymentMethodConfig
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdyenBancontactResumeHandlerTest {
    private lateinit var aydenBancontactResumeHandler: AydenBancontactResumeHandler
    private val clientTokenParser = mockk<AdyenBancontactPaymentMethodClientTokenParser>()
    private val tokenizedPaymentMethodRepository = mockk<TokenizedPaymentMethodRepository>()
    private val configurationRepository = mockk<ConfigurationRepository>()
    private val deeplinkRepository = mockk<RedirectDeeplinkRepository>()
    private val validateClientTokenRepository = mockk<ValidateClientTokenRepository>()
    private val clientTokenRepository = mockk<ClientTokenRepository>()
    private val checkoutAdditionalInfoHandler = mockk<CheckoutAdditionalInfoHandler>()

    @BeforeEach
    fun setUp() {
        aydenBancontactResumeHandler =
            AydenBancontactResumeHandler(
                clientTokenParser = clientTokenParser,
                tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
                configurationRepository = configurationRepository,
                deeplinkRepository = deeplinkRepository,
                validateClientTokenRepository = validateClientTokenRepository,
                clientTokenRepository = clientTokenRepository,
                checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler,
            )
    }

    @Test
    fun `getResumeDecision should the AydenBancontactDecision filled up with the correct data`() {
        // Arrange
        val paymentMethodType = "AYDEN_BANCONTACT"
        val clientToken =
            AdyenBancontactClientToken(
                redirectUrl = "http://redirect.url",
                statusUrl = "http://status.url",
                clientTokenIntent = "clientTokenIntent",
            )
        val paymentMethodConfig =
            PaymentMethodConfig(
                id = "123",
                type = paymentMethodType,
                name = "Ayden Bancontact",
                options = null,
            )
        val paymentMethod =
            mockk<PaymentMethodTokenInternal> {
                every { this@mockk.paymentMethodType } returns paymentMethodType
            }
        val config =
            mockk<Configuration> {
                every { paymentMethods } returns listOf(paymentMethodConfig)
            }

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod
        every { configurationRepository.getConfiguration() } returns config
        every { deeplinkRepository.getDeeplinkUrl() } returns "http://deeplink.url"

        runTest {
            // Act
            val result = aydenBancontactResumeHandler.getResumeDecision(clientToken)

            // Assert
            assertEquals("Ayden Bancontact", result.title)
            assertEquals(paymentMethodType, result.paymentMethodType)
            assertEquals("http://redirect.url", result.redirectUrl)
            assertEquals("http://status.url", result.statusUrl)
            assertEquals("http://deeplink.url", result.deeplinkUrl)
        }
    }

    @Test
    fun `supportedClientTokenIntents should return the correct data`() {
        // Arrange
        val paymentMethodType = "AYDEN_BANCONTACT"
        val paymentMethod =
            mockk<PaymentMethodTokenInternal> {
                every { this@mockk.paymentMethodType } returns paymentMethodType
            }

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod

        // Act
        val result = aydenBancontactResumeHandler.supportedClientTokenIntents()

        // Assert
        assertEquals(listOf("AYDEN_BANCONTACT_REDIRECTION"), result)
    }
}
