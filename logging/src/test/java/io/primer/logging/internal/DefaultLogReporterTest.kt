package io.primer.logging.internal

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.primer.android.core.logging.PrimerLogLevel
import io.primer.android.core.logging.PrimerLogger
import io.primer.android.core.logging.PrimerLogging
import io.primer.android.core.logging.internal.DefaultLogReporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class DefaultLogReporterTest {
    private lateinit var logReporter: DefaultLogReporter

    @RelaxedMockK
    private lateinit var logger: PrimerLogger

    @BeforeEach
    fun setUp() {
        logReporter = DefaultLogReporter()
    }

    @Test
    fun `should not log any logs when PrimerLogLevel is set to NONE`() {
        every { logger.logLevel } returns PrimerLogLevel.NONE
        PrimerLogging.logger = logger

        reportLogs()

        verifyNumberOfCalls(0)
    }

    @Test
    fun `should log Debug, Info, Warning and Error logs when PrimerLogLevel is set to DEBUG`() {
        every { logger.logLevel } returns PrimerLogLevel.DEBUG
        PrimerLogging.logger = logger

        reportLogs()

        verifyNumberOfCalls(4)
    }

    @Test
    fun `should log only Info, Warning and Error logs when PrimerLogLevel is set to INFO`() {
        PrimerLogging.logger = logger
        every { logger.logLevel } returns PrimerLogLevel.INFO

        reportLogs()

        verifyNumberOfCalls(3)
    }

    @Test
    fun `should log only Warning and Error logs when PrimerLogLevel is set to WARNING`() {
        PrimerLogging.logger = logger
        every { logger.logLevel } returns PrimerLogLevel.WARNING

        reportLogs()

        verifyNumberOfCalls(2)
    }

    @Test
    fun `should log only Error logs when PrimerLogLevel is set to ERROR`() {
        PrimerLogging.logger = logger
        every { logger.logLevel } returns PrimerLogLevel.ERROR

        reportLogs()

        verifyNumberOfCalls(1)
    }

    private fun reportLogs() {
        logReporter.apply {
            debug(TEST_MESSAGE)
            info(TEST_MESSAGE)
            warn(TEST_MESSAGE)
            error(TEST_MESSAGE)
        }
    }

    private fun verifyNumberOfCalls(numberOfCalls: Int) {
        verify(exactly = numberOfCalls) {
            logger.log(any())
        }
    }

    private companion object {
        const val TEST_MESSAGE = "test"
    }
}
