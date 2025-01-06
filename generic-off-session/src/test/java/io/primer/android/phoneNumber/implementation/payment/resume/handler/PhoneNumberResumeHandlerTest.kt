package io.primer.android.phoneNumber.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data.PhoneNumberClientTokenParser
import io.primer.android.phoneNumber.implementation.payment.resume.domain.model.PhoneNumberClientToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PhoneNumberResumeHandlerTest {
    private lateinit var clientTokenParser: PhoneNumberClientTokenParser
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var handler: PhoneNumberResumeHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        tokenizedPaymentMethodRepository = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()
        handler =
            PhoneNumberResumeHandler(
                clientTokenParser,
                tokenizedPaymentMethodRepository,
                validateClientTokenRepository,
                clientTokenRepository,
                checkoutAdditionalInfoHandler,
            )
    }

    @Test
    fun `supportedClientTokenIntents should return the correct data`() {
        // Arrange
        val paymentMethodType = "OMISE_PROMPTPAY"
        val paymentMethod =
            mockk<PaymentMethodTokenInternal> {
                every { this@mockk.paymentMethodType } returns paymentMethodType
            }

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod

        // Act
        val result = handler.supportedClientTokenIntents()

        // Assert
        assertEquals(listOf("OMISE_PROMPTPAY_REDIRECTION"), result)
    }

    @Test
    fun `getResumeDecision should correctly parse client token and return PhoneNumberDecision`() {
        val clientToken =
            PhoneNumberClientToken(
                clientTokenIntent = "PAYMENT_METHOD_VOUCHER",
                statusUrl = "statusUrl",
            )

        val expectedDecision =
            PhoneNumberDecision(
                statusUrl = clientToken.statusUrl,
            )

        runTest {
            val result = handler.getResumeDecision(clientToken)
            assertEquals(expectedDecision, result)
        }
    }
}
