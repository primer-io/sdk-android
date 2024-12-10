package io.primer.android.klarna.implementation.errors.data.mapper

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaSdkErrorException
import io.primer.android.klarna.implementation.errors.data.exception.KlarnaUserUnapprovedException
import io.primer.android.klarna.implementation.errors.domain.model.KlarnaError
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KlarnaErrorMapperTest {
    private val errorMapper = KlarnaErrorMapper()

    @Test
    fun `getPrimerError should return UserUnapprovedError when throwable is KlarnaUserUnapprovedException`() {
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
    fun `getPrimerError should return KlarnaSdkError when throwable is KlarnaSdkErrorException`() {
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
    fun `getPrimerError should throw IllegalArgumentException when it receives unsupported exceptions`() {
        // Given
        val exception = IllegalStateException("Some error")

        // When / Then
        val thrown = assertThrows(IllegalStateException::class.java) {
            errorMapper.getPrimerError(exception)
        }
        Assertions.assertEquals(
            "Unsupported mapping for java.lang.IllegalStateException: Some error " +
                "in io.primer.android.klarna.implementation.errors.data.mapper.KlarnaErrorMapper",
            thrown.message
        )
    }
}
