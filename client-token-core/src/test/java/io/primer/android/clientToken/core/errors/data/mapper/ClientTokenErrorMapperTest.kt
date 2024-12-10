package io.primer.android.clientToken.core.errors.data.mapper

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.errors.domain.models.ClientTokenError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ClientTokenErrorMapperTest {

    private val mapper = ClientTokenErrorMapper()

    @Test
    fun `test mapping InvalidClientTokenException to InvalidClientTokenError`() {
        // Arrange
        val exception = InvalidClientTokenException()

        // Act
        val result = mapper.getPrimerError(exception)

        // Assert
        assertEquals(ClientTokenError.InvalidClientTokenError(description = exception.message), result)
    }

    @Test
    fun `test mapping ExpiredClientTokenException to ExpiredClientTokenError`() {
        // Arrange
        val exception = ExpiredClientTokenException()

        // Act
        val result = mapper.getPrimerError(exception)

        // Assert
        assertEquals(ClientTokenError.ExpiredClientTokenError(description = exception.message), result)
    }

    @Test
    fun `test unsupported exception mapping throws error`() {
        // Arrange
        val unsupportedException = IllegalArgumentException("Unsupported exception")

        // Act & Assert
        val exception = assertThrows<IllegalStateException> {
            mapper.getPrimerError(unsupportedException)
        }
        assertEquals(
            "Unsupported mapping for java.lang.IllegalArgumentException: Unsupported exception " +
                "in io.primer.android.clientToken.core.errors.data.mapper.ClientTokenErrorMapper",
            exception.message
        )
    }
}
