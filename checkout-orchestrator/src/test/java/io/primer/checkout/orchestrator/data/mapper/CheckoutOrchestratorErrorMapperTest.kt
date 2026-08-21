package io.primer.checkout.orchestrator.data.mapper

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorError
import io.primer.checkout.orchestrator.domain.error.CheckoutOrchestratorException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertTrue

class CheckoutOrchestratorErrorMapperTest {

    private val errorMapper = CheckoutOrchestratorErrorMapper()

    @BeforeEach
    fun setUp() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "UUID"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class)
    }

    @Test
    fun `getPrimerError should return StateProcessorError when throwable is StateProcessorException`() {
        val throwable = CheckoutOrchestratorException.StateProcessorException(
            code = "ERROR_CODE",
            stateProcessorDiagnosticsId = "diag-123",
            stateProcessorMessage = "Something went wrong",
        )

        val actualResult = errorMapper.getPrimerError(throwable)

        assertTrue(actualResult is CheckoutOrchestratorError.StateProcessorError)
        val error = actualResult as CheckoutOrchestratorError.StateProcessorError
        assertEquals("ERROR_CODE", error.code)
        assertEquals("diag-123", error.stateProcessorDiagnosticsId)
        assertEquals("Something went wrong", error.stateProcessorMessage)
        assertEquals("state-processor-error", error.errorId)
        assertEquals(
            "State processor error [ERROR_CODE] (diagnosticsId=diag-123): Something went wrong",
            error.description,
        )
    }

    @Test
    fun `getPrimerError should return CheckoutTerminalError when throwable is TerminalErrorException`() {
        val throwable = CheckoutOrchestratorException.TerminalErrorException()

        val actualResult = errorMapper.getPrimerError(throwable)

        assertTrue(actualResult is CheckoutOrchestratorError.CheckoutTerminalError)
        assertEquals("checkout-terminal-error", actualResult.errorId)
        assertEquals("Checkout ended with an error outcome.", actualResult.description)
    }

    @Test
    fun `getPrimerError should throw IllegalStateException when it receives unsupported exceptions`() {
        val exception = IllegalStateException("Some error")

        val thrown = assertThrows(IllegalStateException::class.java) {
            errorMapper.getPrimerError(exception)
        }
        assertEquals(
            "Unsupported mapping for java.lang.IllegalStateException: Some error " +
                "in io.primer.checkout.orchestrator.data.mapper.CheckoutOrchestratorErrorMapper",
            thrown.message,
        )
    }
}
