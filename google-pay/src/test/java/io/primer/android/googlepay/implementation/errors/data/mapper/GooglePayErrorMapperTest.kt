package io.primer.android.googlepay.implementation.errors.data.mapper

import com.google.android.gms.common.api.Status
import io.primer.android.googlepay.implementation.errors.domain.exception.GooglePayException
import io.primer.android.googlepay.implementation.errors.domain.model.GooglePayError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayErrorMapperTest {

    private lateinit var errorMapper: GooglePayErrorMapper

    @BeforeEach
    fun setUp() {
        errorMapper = GooglePayErrorMapper()
    }

    @Test
    fun `getPrimerError should return GooglePayInternalError when it receives a GooglePayException`() {
        // Given
        val status = Status.RESULT_SUCCESS
        val exception = GooglePayException(status)

        // When
        val result = errorMapper.getPrimerError(exception)

        // Then
        val expected = GooglePayError.GooglePayInternalError(status)
        assertEquals(expected, result)
    }

    @Test
    fun `getPrimerError should throw IllegalArgumentException when it receives unsupported exceptions`() {
        // Given
        val exception = IllegalStateException("Some error")

        // When / Then
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            errorMapper.getPrimerError(exception)
        }
        assertEquals("Unsupported mapping for java.lang.IllegalStateException: Some error", thrown.message)
    }
}
