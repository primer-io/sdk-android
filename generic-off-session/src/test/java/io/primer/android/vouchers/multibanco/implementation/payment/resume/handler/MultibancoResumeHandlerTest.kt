package io.primer.android.vouchers.multibanco.implementation.payment.resume.handler

import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data.MultibancoClientTokenParser
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.domain.model.MultibancoClientToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal class MultibancoResumeHandlerTest {
    private lateinit var clientTokenParser: MultibancoClientTokenParser
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var handler: MultibancoResumeHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()
        handler =
            MultibancoResumeHandler(
                clientTokenParser,
                validateClientTokenRepository,
                clientTokenRepository,
                checkoutAdditionalInfoHandler,
            )
    }

    @Test
    fun `supportedClientTokenIntents should return correct intents`() {
        val expectedIntents = listOf("PAYMENT_METHOD_VOUCHER")
        val result = handler.supportedClientTokenIntents()
        assertEquals(expectedIntents, result)
    }

    @Test
    fun `getResumeDecision should correctly parse client token and return MultibancoDecision`() {
        val clientToken =
            MultibancoClientToken(
                clientTokenIntent = "PAYMENT_METHOD_VOUCHER",
                expiresAt = "2024-07-10T00:00:00",
                reference = "some-reference",
                entity = "some-entity",
            )

        val expiresDateFormat =
            DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT,
            )

        val expectedDecision =
            MultibancoDecision(
                expiresAt =
                expiresDateFormat.format(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(clientToken.expiresAt),
                ),
                reference = clientToken.reference,
                entity = clientToken.entity,
            )

        runTest {
            val result = handler.getResumeDecision(clientToken)
            assertEquals(expectedDecision, result)
        }
    }
}
