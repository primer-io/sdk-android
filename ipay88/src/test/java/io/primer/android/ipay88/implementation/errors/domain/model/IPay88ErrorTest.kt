package io.primer.android.ipay88.implementation.errors.domain.model

import io.mockk.every
import io.mockk.mockkStatic
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IPay88ErrorTest {

    private val transactionId = "transId123"
    private val refNo = "refNo123"
    private val errorDescription = "Error occurred"
    private val fixedDiagnosticsId = "fixed-diagnostics-id"
    private lateinit var error: IPay88Error.IPaySdkPaymentFailedError

    @BeforeEach
    fun setUp() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns fixedDiagnosticsId
        error = IPay88Error.IPaySdkPaymentFailedError(transactionId, refNo, errorDescription)
    }

    @Test
    fun `IPaySdkPaymentFailedError should return correct errorId`() {
        val error = IPay88Error.IPaySdkPaymentFailedError("transId123", "refNo123", "Error occurred")
        assertEquals("payment-failed", error.errorId)
    }

    @Test
    fun `IPaySdkPaymentFailedError should return correct description`() {
        val expectedDescription = """
            iPay88 payment (transId: $transactionId, refNo: $refNo
            failed with error: $errorDescription 
            diagnosticsId: ${error.diagnosticsId})  
        """.trimIndent()
        assertEquals(expectedDescription, error.description)
    }

    @Test
    fun `IPaySdkConnectionError should return correct errorId`() {
        val error = IPay88Error.IPaySdkConnectionError
        assertEquals("ipay-sdk-connection-error", error.errorId)
    }

    @Test
    fun `IPaySdkConnectionError should return correct description`() {
        val error = IPay88Error.IPaySdkConnectionError
        val expectedDescription = "IPay SDK connection error occurred: (diagnosticsId: ${error.diagnosticsId})"
        assertEquals(expectedDescription, error.description)
    }

    @Test
    fun `IPaySdkPaymentFailedError should return correct context`() {
        val error = IPay88Error.IPaySdkPaymentFailedError("transId123", "refNo123", "Error occurred")
        val expectedContext = ErrorContextParams("payment-failed", PaymentMethodType.IPAY88_CARD.name)
        assertEquals(expectedContext, error.context)
    }

    @Test
    fun `IPaySdkConnectionError should return correct context`() {
        val error = IPay88Error.IPaySdkConnectionError
        val expectedContext = ErrorContextParams("ipay-sdk-connection-error", PaymentMethodType.IPAY88_CARD.name)
        assertEquals(expectedContext, error.context)
    }

    @Test
    fun `IPaySdkPaymentFailedError should return null recoverySuggestion`() {
        val error = IPay88Error.IPaySdkPaymentFailedError("transId123", "refNo123", "Error occurred")
        assertEquals(null, error.recoverySuggestion)
    }

    @Test
    fun `IPaySdkConnectionError should return null recoverySuggestion`() {
        val error = IPay88Error.IPaySdkConnectionError
        assertEquals(null, error.recoverySuggestion)
    }
}
