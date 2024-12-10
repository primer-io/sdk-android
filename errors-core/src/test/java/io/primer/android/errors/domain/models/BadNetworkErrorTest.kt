import io.primer.android.errors.domain.models.BadNetworkError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BadNetworkErrorTest {

    @Test
    fun `should create BadNetworkError with given diagnostics ID`() {
        val diagnosticsId = "12345"

        val error = BadNetworkError(diagnosticsId)

        assertEquals("bad-network", error.errorId)
        assertEquals("Failed to perform network request because internet connection is bad.", error.description)
        assertEquals(diagnosticsId, error.diagnosticsId)
        assertEquals("Check the internet connection and try again.", error.recoverySuggestion)
        assertEquals(error, error.exposedError)
        assertEquals(null, error.errorCode)
    }
}
