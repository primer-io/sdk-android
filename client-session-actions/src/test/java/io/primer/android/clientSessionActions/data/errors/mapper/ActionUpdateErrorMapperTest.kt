package io.primer.android.clientSessionActions.data.errors.mapper

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientSessionActions.domain.errors.model.ActionUpdateFailedError
import io.primer.android.errors.data.exception.SessionUpdateException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ActionUpdateErrorMapperTest {
    private lateinit var errorMapper: ActionUpdateErrorMapper

    @BeforeEach
    fun setUp() {
        errorMapper = ActionUpdateErrorMapper()
    }

    @Test
    fun `getPrimerError should map SessionUpdateException to ActionUpdateFailedError`() {
        val diagnosticsId = "diagnosticsId"
        val description = "description"
        val httpException: SessionUpdateException =
            mockk {
                every { this@mockk.diagnosticsId } returns diagnosticsId
                every { this@mockk.description } returns description
            }

        val result = errorMapper.getPrimerError(httpException)

        assertTrue(result is ActionUpdateFailedError)

        val actionUpdateFailedError = result.exposedError as ActionUpdateFailedError
        assertEquals(description, actionUpdateFailedError.description)
        assertEquals(diagnosticsId, actionUpdateFailedError.diagnosticsId)
    }

    @Test
    fun `getPrimerError should throw IllegalArgumentException for unsupported exceptions`() {
        val unsupportedException = IllegalArgumentException("Unsupported exception")

        val exception =
            assertThrows<IllegalArgumentException> {
                errorMapper.getPrimerError(unsupportedException)
            }

        assertEquals("Unsupported mapping for $unsupportedException", exception.message)
    }
}
