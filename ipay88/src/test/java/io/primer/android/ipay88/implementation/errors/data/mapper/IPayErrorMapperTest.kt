package io.primer.android.ipay88.implementation.errors.data.mapper

import io.mockk.MockKAnnotations
import io.mockk.junit5.MockKExtension
import io.primer.android.ipay88.implementation.errors.domain.model.IPay88Error
import io.primer.ipay88.api.exceptions.IPayConnectionErrorException
import io.primer.ipay88.api.exceptions.IPayPaymentFailedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class IPayErrorMapperTest {
    // Instantiate the class to be tested
    private lateinit var errorMapper: IPayErrorMapper

    @BeforeEach
    fun setUp() {
        // Initialize MockK annotations and instantiate the errorMapper
        MockKAnnotations.init(this)
        errorMapper = IPayErrorMapper()
    }

    @Test
    fun `getPrimerError should return IPaySdkPaymentFailedError for IPayPaymentFailedException`() {
        // Arrange
        val transactionId = "12345"
        val refNo = "refNo123"
        val tokenId = "tokenId123"
        val errorDescription = "Payment failed"
        val exception = IPayPaymentFailedException(transactionId, tokenId, refNo, errorDescription)

        // Act
        val result = errorMapper.getPrimerError(exception)

        // Assert
        val expected = IPay88Error.IPaySdkPaymentFailedError(transactionId, refNo, errorDescription)
        assertEquals(expected, result)
    }

    @Test
    fun `getPrimerError should return IPaySdkConnectionError for IPayConnectionErrorException`() {
        // Arrange
        val exception = IPayConnectionErrorException()

        // Act
        val result = errorMapper.getPrimerError(exception)

        // Assert
        assertEquals(IPay88Error.IPaySdkConnectionError, result)
    }

    @Test
    fun `getPrimerError should throw an error for unsupported exceptions`() {
        // Arrange
        val exception = IllegalArgumentException("Unsupported exception")

        // Act & Assert
        val error =
            assertFailsWith<IllegalStateException> {
                errorMapper.getPrimerError(exception)
            }
        assertEquals(
            expected =
                "Unsupported mapping for $exception in " +
                    "io.primer.android.ipay88.implementation.errors.data.mapper.IPayErrorMapper",
            actual = error.message,
        )
    }
}
