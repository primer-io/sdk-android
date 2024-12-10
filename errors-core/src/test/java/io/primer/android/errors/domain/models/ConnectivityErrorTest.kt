import io.primer.android.errors.domain.models.ConnectivityError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectivityErrorTest {

    @Test
    fun `should create ConnectivityError with given message`() {
        // Arrange
        val message = "Network connection is unstable."

        // Act
        val error = ConnectivityError(message)

        // Assert
        assertEquals("connectivity-errors", error.errorId)
        assertEquals(message, error.description)
        assertNotNull(error.diagnosticsId)
        assertTrue(error.diagnosticsId.isNotBlank())
        assertEquals("Please check underlying errors to investigate further.", error.recoverySuggestion)
        assertNull(error.errorCode)
    }
}
