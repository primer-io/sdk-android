package io.primer.android.errors.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.models.PrimerError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BaseErrorResolverTest {
    private lateinit var errorMapperRegistry: ErrorMapperRegistry
    private lateinit var logReporter: LogReporter
    private lateinit var baseErrorResolver: BaseErrorResolver

    @BeforeEach
    fun setUp() {
        errorMapperRegistry = mockk()
        logReporter =
            mockk {
                every { error(any()) } returns Unit
            }
        baseErrorResolver = DefaultErrorResolver(errorMapperRegistry, logReporter)
    }

    @Test
    fun `resolve should log error`() {
        // Arrange
        val throwable = RuntimeException("Test error")
        val primerError =
            mockk<PrimerError> {
                every { errorId } returns "test-error"
                every { description } returns "Test error occurred"
                every { diagnosticsId } returns "123456"
                every { context } returns null // Mock context as needed
                every { exposedError } returns this
            }
        every { errorMapperRegistry.getPrimerError(throwable) } returns primerError

        // Act
        baseErrorResolver.resolve(throwable)

        // Assert
        verify { logReporter.error("SDK encountered an error: [test-error] Test error occurred") }
    }
}
