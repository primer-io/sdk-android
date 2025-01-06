package io.primer.android.ipay88.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.primer.android.PrimerSessionIntent
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.configuration.data.model.CustomerDataResponse
import io.primer.android.configuration.data.model.OrderDataResponse
import io.primer.android.configuration.domain.model.ClientSession
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.model.PaymentMethodConfig
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.core.utils.BaseDataWithInputProvider
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.implementation.deeplink.domain.repository.IPay88DeeplinkRepository
import io.primer.android.ipay88.implementation.payment.resume.clientToken.data.IPay88ClientTokenParser
import io.primer.android.ipay88.implementation.payment.resume.clientToken.domain.model.IPay88ClientToken
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import io.primer.android.ipay88.implementation.validation.resolvers.IPay88ValidationRulesResolver
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IPay88ResumeHandlerTest {
    // Mock dependencies
    private val iPay88DeeplinkRepository: IPay88DeeplinkRepository = mockk()
    private val iPay88ValidationRulesResolver: IPay88ValidationRulesResolver = mockk()
    private val clientTokenParser: IPay88ClientTokenParser = mockk()
    private val tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository = mockk()
    private val configurationRepository: ConfigurationRepository = mockk(relaxed = true)
    private val validateClientTokenRepository: ValidateClientTokenRepository = mockk()
    private val formattedAmountProvider: BaseDataWithInputProvider<MonetaryAmount, String> = mockk()
    private val clientTokenRepository: ClientTokenRepository = mockk()
    private val checkoutAdditionalInfoHandler = mockk<CheckoutAdditionalInfoHandler>()

    // Class under test
    private lateinit var handler: IPay88ResumeHandler

    @BeforeEach
    fun setUp() {
        handler =
            IPay88ResumeHandler(
                iPay88DeeplinkRepository,
                iPay88ValidationRulesResolver,
                clientTokenParser,
                tokenizedPaymentMethodRepository,
                configurationRepository,
                validateClientTokenRepository,
                formattedAmountProvider,
                clientTokenRepository,
                checkoutAdditionalInfoHandler,
            )
        mockkStatic(ClientSessionDataResponse::class)
    }

    @Test
    fun `getResumeDecision should return correct decision when validation succeeds`() {
        // Mock necessary dependencies and their behavior
        val clientToken =
            IPay88ClientToken(
                intent = "intent",
                statusUrl = "statusUrl",
                paymentId = "paymentId",
                paymentMethod = 1,
                actionType = "actionType",
                referenceNumber = "referenceNumber",
                supportedCurrencyCode = "USD",
                backendCallbackUrl = "callbackUrl",
                supportedCountryCode = "US",
                clientTokenIntent = "intent",
            )

        val customerFullName = "John Doe"
        val customerEmailAddress = "john.doe@example.com"
        val customerId = "123456"

        val paymentMethodConfiguration =
            mockk<PaymentMethodConfig>(relaxed = true) {
                every { type } returns "credit_card"
                every { options } returns
                    mockk {
                        every { merchantId } returns "merchant123"
                    }
            }

        val configurationMock =
            mockk<Configuration>(relaxed = true) {
                every { paymentMethods } returns listOf(paymentMethodConfiguration)
            }
        val orderMock =
            mockk<OrderDataResponse> {
                every { currencyCode } returns "USD"
                every { countryCode } returns CountryCode.US
                every { currentAmount } returns 100
                every { lineItems } returns
                    listOf(
                        mockk {
                            every { description } returns "description"
                        },
                    )
            }
        val clientSessionDataMock = mockk<ClientSessionDataResponse>()
        val clientSessionMock = mockk<ClientSession>()
        val customerMock = mockk<CustomerDataResponse>()

        every { configurationRepository.getConfiguration() } returns configurationMock
        every { configurationMock.clientSession } returns clientSessionMock
        every { clientSessionMock.clientSessionDataResponse } returns clientSessionDataMock
        every { clientSessionDataMock.customer } returns customerMock
        every { clientSessionDataMock.order } returns orderMock
        every { clientSessionDataMock.toClientSessionData() } returns mockk()

        every { customerMock.getFullName() } returns customerFullName
        every { customerMock.emailAddress } returns customerEmailAddress
        every { customerMock.customerId } returns customerId

        every { formattedAmountProvider.provide(any()) } returns "$100.00"

        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns "credit_card"
        every { iPay88DeeplinkRepository.getDeeplinkUrl() } returns "https://example.com/deeplink"

        // Mock validation rules resolver
        val validationRuleMock = mockk<ValidationRule<IPay88ValidationData>>()
        val validationResult = ValidationResult.Success

        every { iPay88ValidationRulesResolver.resolve().rules } returns listOf(validationRuleMock)
        every { validationRuleMock.validate(any()) } returns validationResult

        runTest {
            // Invoke the method under test
            val decision = handler.getResumeDecision(clientToken)

            // Verify the result
            assertEquals("statusUrl", decision.statusUrl)
            assertEquals("paymentId", decision.iPayPaymentId)
            assertEquals(1, decision.iPayMethod)
            assertEquals("merchant123", decision.merchantCode)
            assertEquals("actionType", decision.actionType)
            assertEquals("$100.00", decision.amount)
            assertEquals("referenceNumber", decision.referenceNumber)
            assertEquals("description", decision.prodDesc)
            assertEquals("USD", decision.currencyCode)
            assertEquals("US", decision.countryCode)
            assertEquals("John Doe", decision.customerName)
            assertEquals("john.doe@example.com", decision.customerEmail)
            assertEquals("123456", decision.remark)
            assertEquals("callbackUrl", decision.backendCallbackUrl)
            assertEquals("https://example.com/deeplink", decision.deeplinkUrl)
            assertEquals(1234, decision.errorCode)
            assertEquals("credit_card", decision.paymentMethodType)
            assertEquals(PrimerSessionIntent.CHECKOUT, decision.sessionIntent)
        }
    }

    @Test
    fun `getResumeDecision should throw IllegalClientSessionValueException when validation fails`() {
        // Mock necessary dependencies and their behavior

        val customerFullName = "John Doe"
        val customerEmailAddress = "john.doe@example.com"
        val customerId = "123456"

        val paymentMethodConfiguration =
            mockk<PaymentMethodConfig>(relaxed = true) {
                every { type } returns "credit_card"
                every { options } returns
                    mockk {
                        every { merchantId } returns "merchant123"
                    }
            }

        val configurationMock =
            mockk<Configuration>(relaxed = true) {
                every { paymentMethods } returns listOf(paymentMethodConfiguration)
            }
        val orderMock =
            mockk<OrderDataResponse> {
                every { currencyCode } returns "USD"
                every { countryCode } returns CountryCode.US
                every { currentAmount } returns 100
                every { lineItems } returns
                    listOf(
                        mockk {
                            every { description } returns "description"
                        },
                    )
            }
        val clientSessionDataMock = mockk<ClientSessionDataResponse>()
        val clientSessionMock = mockk<ClientSession>()
        val customerMock = mockk<CustomerDataResponse>()

        every { configurationRepository.getConfiguration() } returns configurationMock
        every { configurationMock.clientSession } returns clientSessionMock
        every { clientSessionMock.clientSessionDataResponse } returns clientSessionDataMock
        every { clientSessionDataMock.customer } returns customerMock
        every { clientSessionDataMock.order } returns orderMock
        every { clientSessionDataMock.toClientSessionData() } returns mockk()

        every { customerMock.getFullName() } returns customerFullName
        every { customerMock.emailAddress } returns customerEmailAddress
        every { customerMock.customerId } returns customerId

        every { formattedAmountProvider.provide(any()) } returns "$100.00"

        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns "credit_card"
        every { iPay88DeeplinkRepository.getDeeplinkUrl() } returns "https://example.com/deeplink"

        // Mock validation rules resolver
        val validationRuleMock = mockk<ValidationRule<IPay88ValidationData>>()
        val mockk = mockk<IllegalClientSessionValueException>()
        val validationResult = ValidationResult.Failure(mockk)

        every { iPay88ValidationRulesResolver.resolve().rules } returns listOf(validationRuleMock)
        every { validationRuleMock.validate(any()) } returns validationResult

        // Assert that IllegalClientSessionValueException is thrown
        assertThrows(IllegalClientSessionValueException::class.java) {
            runTest {
                handler.getResumeDecision(mockk())
            }
        }
    }

    @Test
    fun `supportedClientTokenIntents should return list of client token intents`() {
        // Mock the tokenizedPaymentMethodRepository to return a payment method type
        val paymentMethod =
            mockk<PaymentMethodTokenInternal> {
                every { paymentMethodType } returns "credit_card"
            }
        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod

        // Invoke the property
        val result = handler.supportedClientTokenIntents()

        // Assert the result
        assertEquals(listOf("credit_card_REDIRECTION"), result)
    }
}
