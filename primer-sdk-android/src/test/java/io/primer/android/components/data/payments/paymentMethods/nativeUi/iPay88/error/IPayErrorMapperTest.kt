package io.primer.android.components.data.payments.paymentMethods.nativeUi.iPay88.error

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.IPay88Error
import io.primer.ipay88.api.exceptions.IPayConnectionErrorException
import io.primer.ipay88.api.exceptions.IPayPaymentFailedException
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IPayErrorMapperTest {
    private val errorMapper = IPayErrorMapper()

    @Test
    fun `should emit IPaySdkPaymentFailedError when throwable is IPayPaymentFailedException`() {
        val paymentMethodType = PaymentMethodType.IPAY88_CARD
        val transactionId = "transactionId"
        val refNo = "refNo"
        val errorDescription = "errorDescription"
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
        val throwable =
            IPayPaymentFailedException(transactionId, "tokenId", refNo, errorDescription)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            """
               iPay88 payment (transId: $transactionId, refNo: $refNo
               failed with error: $errorDescription 
               diagnosticsId: ${UUID.randomUUID()})  
            """.trimIndent()
        val expectedContext = ErrorContextParams(
            "payment-failed",
            paymentMethodType.name
        )

        assertTrue(actualResult is IPay88Error.IPaySdkPaymentFailedError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
        unmockkStatic(UUID::class)
    }

    @Test
    fun `should emit IPaySdkConnectionError when throwable is IPayConnectionErrorException`() {
        val paymentMethodType = PaymentMethodType.IPAY88_CARD
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
        val throwable = IPayConnectionErrorException()

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "IPay SDK connection error occurred: (diagnosticsId: UUID)"
        val expectedContext = ErrorContextParams(
            "ipay-sdk-connection-error",
            paymentMethodType.name
        )

        assertTrue(actualResult is IPay88Error.IPaySdkConnectionError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
        unmockkStatic(UUID::class)
    }

    @Test
    fun `should emit GeneralError when throwable is not known mapped Exception`() {
        val message = "message"
        val throwable = Exception(message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription = "Something went wrong. Message $message."

        assertTrue(actualResult is GeneralError.UnknownError)
        assertEquals(expectedDescription, actualResult.description)
    }
}
