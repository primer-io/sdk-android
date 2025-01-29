package io.primer.android.vouchers.retailOutlets.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data.RetailOutletsClientTokenParser
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.domain.model.RetailOutletsClientToken
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

internal class RetailOutletsResumeHandlerTest {
    private lateinit var clientTokenParser: RetailOutletsClientTokenParser
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var retailOutletRepository: RetailOutletRepository
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var handler: RetailOutletsResumeHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        retailOutletRepository = mockk()
        tokenizedPaymentMethodRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()
        handler =
            RetailOutletsResumeHandler(
                clientTokenParser,
                validateClientTokenRepository,
                clientTokenRepository,
                retailOutletRepository,
                tokenizedPaymentMethodRepository,
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
    fun `getResumeDecision should correctly parse client token and return RetailOutletsDecision`() {
        val clientToken =
            RetailOutletsClientToken(
                clientTokenIntent = "PAYMENT_METHOD_VOUCHER",
                expiresAt = "2024-07-10T00:00:00",
                reference = "some-reference",
                entity = "some-entity",
            )

        every { retailOutletRepository.getCachedRetailOutlets() } returns
            listOf(
                mockk<RetailOutlet> {
                    every { id } returns "some-id"
                    every { name } returns "some-name"
                },
            )

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns
            mockk {
                every { paymentInstrumentData?.sessionInfo?.retailOutlet } returns "some-id"
            }

        val expiresDateFormat =
            DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT,
            )

        val expectedDecision =
            RetailOutletsDecision(
                expiresAt =
                expiresDateFormat.format(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(clientToken.expiresAt),
                ),
                reference = clientToken.reference,
                entity = clientToken.entity,
                retailerName =
                retailOutletRepository.getCachedRetailOutlets().first { retailOutlet ->
                    retailOutlet.id ==
                        tokenizedPaymentMethodRepository.getPaymentMethod()
                            .paymentInstrumentData?.sessionInfo?.retailOutlet
                }.name,
            )

        runTest {
            val result = handler.getResumeDecision(clientToken)
            assertEquals(expectedDecision, result)
        }
    }
}
