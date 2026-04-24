package io.primer.executionengine.data.executors.analytics

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.executionengine.domain.models.Outcome
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class PlatformLogStepExecutorTest {

    @MockK
    lateinit var analyticsRepository: AnalyticsRepository

    @MockK
    lateinit var logReporter: LogReporter

    private lateinit var executor: PlatformLogStepExecutor

    @BeforeEach
    fun setUp() {
        executor = PlatformLogStepExecutor(analyticsRepository, logReporter)
    }

    @Test
    fun `execute should add raw event and return SUCCESS`() = runTest {
        val step = """{"eventType":"click","data":"test"}"""
        every { analyticsRepository.addRawEvent(step) } just Runs

        val result = executor.execute("action-1", step)

        assertTrue(result.isSuccess)
        val stepResult = result.getOrThrow()
        assertEquals(Outcome.SUCCESS, stepResult.outcome)
        assertEquals("action-1", stepResult.actionId)
        assertEquals(emptyMap<String, Any?>(), stepResult.data)
        verify { analyticsRepository.addRawEvent(step) }
    }

    @Test
    fun `execute should return SUCCESS even when addRawEvent throws`() = runTest {
        val step = """{"eventType":"click"}"""
        val exception = RuntimeException("analytics failed")
        every { analyticsRepository.addRawEvent(step) } throws exception
        every { logReporter.debug(any()) } just Runs

        val result = executor.execute("action-2", step)

        assertTrue(result.isSuccess)
        assertEquals(Outcome.SUCCESS, result.getOrThrow().outcome)
        verify { logReporter.debug("Failed to log analytics event: analytics failed") }
    }

    @Test
    fun `execute should pass correct actionId to StepResult`() = runTest {
        every { analyticsRepository.addRawEvent(any()) } just Runs

        val result = executor.execute("my-action-id", "{}")

        assertEquals("my-action-id", result.getOrThrow().actionId)
    }
}
