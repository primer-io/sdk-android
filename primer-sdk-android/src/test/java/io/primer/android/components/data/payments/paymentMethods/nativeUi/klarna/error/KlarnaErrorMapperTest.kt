package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.error

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.error.models.GeneralError
import io.primer.android.domain.error.models.KlarnaError
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KlarnaErrorMapperTest {
    private val errorMapper = KlarnaErrorMapper()

    @Test
    fun `should emit UserUnapprovedError when throwable is KlarnaUserUnapprovedException`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
        val throwable = KlarnaUserUnapprovedException()

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "User is not approved to perform Klarna payments (diagnosticsId: ${UUID.randomUUID()})"
        val expectedContext = ErrorContextParams(
            "klarna-user-not-approved",
            PaymentMethodType.KLARNA.name
        )

        assertTrue(actualResult is KlarnaError.UserUnapprovedError)
        assertEquals(expectedDescription, actualResult.description)
        assertEquals(expectedContext, actualResult.context)
        unmockkStatic(UUID::class)
    }

    @Test
    fun `should emit KlarnaSdkError when throwable is KlarnaSdkErrorException`() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
        val message = "message"
        val throwable = KlarnaSdkErrorException(message)

        val actualResult = errorMapper.getPrimerError(throwable)
        val expectedDescription =
            "Multiple errors occurred: $message (diagnosticsId: ${UUID.randomUUID()})"
        val expectedContext = ErrorContextParams(
            "klarna-sdk-error",
            PaymentMethodType.KLARNA.name
        )

        assertTrue(actualResult is KlarnaError.KlarnaSdkError)
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
