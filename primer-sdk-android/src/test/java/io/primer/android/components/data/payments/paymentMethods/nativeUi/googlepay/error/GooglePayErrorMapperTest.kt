package io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.error

import com.google.android.gms.common.api.Status
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.GooglePayError
import io.primer.android.domain.exception.GooglePayException
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GooglePayErrorMapperTest {
    private val errorMapper = GooglePayErrorMapper()

    @Test
    fun `should emit GooglePayInternalError when throwable is GooglePayException`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
        val status = mockk<Status> {
            every { this@mockk.toString() } returns "status"
        }
        val throwable = GooglePayException(status)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Google pay internal error with status: $status (diagnosticsId: ${UUID.randomUUID()})"
        val expectedContext = ErrorContextParams(
            "google-pay-internal",
            PaymentMethodType.GOOGLE_PAY.name
        )

        assertTrue(actualResult is GooglePayError.GooglePayInternalError)
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
