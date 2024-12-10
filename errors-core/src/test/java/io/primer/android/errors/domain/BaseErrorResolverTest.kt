package io.primer.android.errors.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.models.PrimerError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BaseErrorResolverTest {

    private lateinit var errorMapperRegistry: ErrorMapperRegistry
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var logReporter: LogReporter
    private lateinit var baseErrorResolver: BaseErrorResolver

    @BeforeEach
    fun setUp() {
        errorMapperRegistry = mockk()
        analyticsRepository = mockk {
            every { addEvent(any()) } returns Unit
        }
        logReporter = mockk {
            every { error(any()) } returns Unit
        }
        baseErrorResolver = DefaultErrorResolver(analyticsRepository, errorMapperRegistry, logReporter)
    }

    @Test
    fun `resolve should log error and send analytics event`() {
        // Arrange
        val throwable = RuntimeException("Test error")
        val primerError = mockk<PrimerError> {
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
        verify {
            analyticsRepository.addEvent(
                MessageAnalyticsParams(
                    MessageType.ERROR,
                    "Test error occurred",
                    Severity.ERROR,
                    "123456",
                    null
                )
            )
        }
    }
}
