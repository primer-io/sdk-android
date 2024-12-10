package io.primer.android.qrcode.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.qrcode.implementation.payment.resume.clientToken.data.QrCodeClientTokenParser
import io.primer.android.qrcode.implementation.payment.resume.domain.model.QrCodeClientToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal class QrCodeResumeHandlerTest {

    private lateinit var clientTokenParser: QrCodeClientTokenParser
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var handler: QrCodeResumeHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        tokenizedPaymentMethodRepository = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()
        handler = QrCodeResumeHandler(
            clientTokenParser,
            tokenizedPaymentMethodRepository,
            validateClientTokenRepository,
            clientTokenRepository,
            checkoutAdditionalInfoHandler
        )
    }

    @Test
    fun `supportedClientTokenIntents should return the correct data`() {
        // Arrange
        val paymentMethodType = "OMISE_PROMPTPAY"
        val paymentMethod = mockk<PaymentMethodTokenInternal> {
            every { this@mockk.paymentMethodType } returns paymentMethodType
        }

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethod

        // Act
        val result = handler.supportedClientTokenIntents()

        // Assert
        assertEquals(listOf("OMISE_PROMPTPAY_REDIRECTION"), result)
    }

    @Test
    fun `getResumeDecision should correctly parse client token and return QrCodeDecision`() {
        val clientToken = QrCodeClientToken(
            clientTokenIntent = "PAYMENT_METHOD_VOUCHER",
            statusUrl = "statusUrl",
            expiresAt = "2024-07-10T00:00:00",
            qrCodeUrl = "qrCodeUrl",
            qrCodeBase64 = "qrCodeBase64"
        )

        val expiresDateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        )

        val expectedDecision = QrCodeDecision(
            statusUrl = clientToken.statusUrl,
            expiresAt = expiresDateFormat.format(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(clientToken.expiresAt)
            ),
            qrCodeUrl = clientToken.qrCodeUrl,
            qrCodeBase64 = clientToken.qrCodeBase64
        )

        runTest {
            val result = handler.getResumeDecision(clientToken)
            assertEquals(expectedDecision, result)
        }
    }
}
