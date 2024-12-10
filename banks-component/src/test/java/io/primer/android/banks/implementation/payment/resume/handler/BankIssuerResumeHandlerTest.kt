package io.primer.android.banks.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.banks.implementation.payment.resume.clientToken.data.BankIssuerPaymentMethodClientTokenParser
import io.primer.android.banks.implementation.payment.resume.clientToken.domain.model.BankIssuerClientToken
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

class BankIssuerResumeHandlerTest {

    private lateinit var bankIssuerResumeHandler: BankIssuerResumeHandler
    private val clientTokenParser = mockk<BankIssuerPaymentMethodClientTokenParser>()
    private val tokenizedPaymentMethodRepository = mockk<TokenizedPaymentMethodRepository>()
    private val configurationRepository = mockk<ConfigurationRepository>()
    private val deeplinkRepository = mockk<RedirectDeeplinkRepository>()
    private val validateClientTokenRepository = mockk<ValidateClientTokenRepository>()
    private val clientTokenRepository = mockk<ClientTokenRepository>()
    private val checkoutAdditionalInfoHandler = mockk<CheckoutAdditionalInfoHandler>()

    @BeforeEach
    fun setUp() {
        bankIssuerResumeHandler = BankIssuerResumeHandler(
            clientTokenParser = clientTokenParser,
            tokenizedPaymentMethodRepository = tokenizedPaymentMethodRepository,
            configurationRepository = configurationRepository,
            deeplinkRepository = deeplinkRepository,
            validateClientTokenRepository = validateClientTokenRepository,
            clientTokenRepository = clientTokenRepository,
            checkoutAdditionalInfoHandler = checkoutAdditionalInfoHandler
        )
    }

    @Test
    fun `getResumeDecision should the BankIssuerDecision filled up with the correct data`() {
        // Arrange
        val paymentMethodType = "BANK_ISSUER"
        val clientToken = BankIssuerClientToken(
            redirectUrl = "http://redirect.url",
            statusUrl = "http://status.url",
            clientTokenIntent = "clientTokenIntent"
        )
        val paymentMethodConfig = PaymentMethodConfig(
            id = "123",
            type = paymentMethodType,
            name = "Bank Issuer",
            options = null
        )
        val paymentMethod = mockk<PaymentMethodTokenInternal> {
            every { this@mockk.paymentMethodType } returns paymentMethodType
        }
        val config = mockk<Configuration> {
            every { paymentMethods } returns listOf(paymentMethodConfig)
        }

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod
        every { configurationRepository.getConfiguration() } returns config
        every { deeplinkRepository.getDeeplinkUrl() } returns "http://deeplink.url"

        runTest {
            // Act
            val result = bankIssuerResumeHandler.getResumeDecision(clientToken)

            // Assert
            assertEquals("Bank Issuer", result.title)
            assertEquals(paymentMethodType, result.paymentMethodType)
            assertEquals("http://redirect.url", result.redirectUrl)
            assertEquals("http://status.url", result.statusUrl)
            assertEquals("http://deeplink.url", result.deeplinkUrl)
        }
    }

    @Test
    fun `supportedClientTokenIntents should return the correct data`() {
        // Arrange
        val paymentMethodType = "BANK_ISSUER"
        val paymentMethod = mockk<PaymentMethodTokenInternal> {
            every { this@mockk.paymentMethodType } returns paymentMethodType
        }

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod

        // Act
        val result = bankIssuerResumeHandler.supportedClientTokenIntents()

        // Assert
        assertEquals(listOf("BANK_ISSUER_REDIRECTION"), result)
    }
}
