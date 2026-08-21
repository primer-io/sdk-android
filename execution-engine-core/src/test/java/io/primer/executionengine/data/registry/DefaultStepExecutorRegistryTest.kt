package io.primer.executionengine.data.registry

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class DefaultStepExecutorRegistryTest {

    @MockK
    lateinit var logReporter: LogReporter

    @MockK
    lateinit var stepExecutor: StepExecutor

    private lateinit var registry: DefaultStepExecutorRegistry

    @BeforeEach
    fun setUp() {
        every { logReporter.debug(any()) } just Runs
        registry = DefaultStepExecutorRegistry(logReporter)
    }

    @Test
    fun `executeAction should return UNSUPPORTED when no executor registered for type`() = runTest {
        val result = registry.executeAction("action-1", "unknown-type", "{}")

        assertTrue(result.isSuccess)
        val stepResult = result.getOrThrow()
        assertEquals(Outcome.UNSUPPORTED, stepResult.outcome)
        assertEquals("action-1", stepResult.actionId)
        assertEquals(emptyMap<String, Any?>(), stepResult.data)
    }

    @Test
    fun `executeAction should delegate to registered executor`() = runTest {
        val expectedResult = StepResult(
            outcome = Outcome.SUCCESS,
            actionId = "action-1",
            data = mapOf("key" to "value"),
        )
        coEvery { stepExecutor.execute("action-1", """{"param":"test"}""") } returns Result.success(expectedResult)

        registry.registerExecutor("HTTP", stepExecutor)
        val result = registry.executeAction("action-1", "HTTP", """{"param":"test"}""")

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrThrow())
    }

    @Test
    fun `executeAction should return failure when executor fails`() = runTest {
        val exception = RuntimeException("executor failed")
        coEvery { stepExecutor.execute(any(), any()) } returns Result.failure(exception)

        registry.registerExecutor("HTTP", stepExecutor)
        val result = registry.executeAction("action-1", "HTTP", "{}")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `executeAction should log debug message on success`() = runTest {
        val expectedResult = StepResult(
            outcome = Outcome.SUCCESS,
            actionId = "action-1",
        )
        coEvery { stepExecutor.execute(any(), any()) } returns Result.success(expectedResult)

        registry.registerExecutor("HTTP", stepExecutor)
        registry.executeAction("action-1", "HTTP", "{}")

        verify { logReporter.debug("Executing step type='HTTP' actionId='action-1'") }
        verify {
            logReporter.debug("Step type='HTTP' actionId='action-1' completed with outcome=SUCCESS")
        }
    }

    @Test
    fun `executeAction should log debug message on executor failure`() = runTest {
        val exception = RuntimeException("something went wrong")
        coEvery { stepExecutor.execute(any(), any()) } returns Result.failure(exception)

        registry.registerExecutor("HTTP", stepExecutor)
        registry.executeAction("action-1", "HTTP", "{}")

        verify { logReporter.debug("Step type='HTTP' actionId='action-1' failed: something went wrong") }
    }

    @Test
    fun `registerExecutor should replace existing executor for same type`() = runTest {
        val secondExecutor = io.mockk.mockk<StepExecutor>()
        val expectedResult = StepResult(outcome = Outcome.SUCCESS, actionId = "a")
        coEvery { secondExecutor.execute(any(), any()) } returns Result.success(expectedResult)

        registry.registerExecutor("HTTP", stepExecutor)
        registry.registerExecutor("HTTP", secondExecutor)
        val result = registry.executeAction("a", "HTTP", "{}")

        assertEquals(expectedResult, result.getOrThrow())
    }

    @Test
    fun `onFinish should fan out to every registered executor`() {
        var firstFinishCount = 0
        var secondFinishCount = 0
        registry.registerExecutor("first", finishTrackingExecutor { firstFinishCount++ })
        registry.registerExecutor("second", finishTrackingExecutor { secondFinishCount++ })

        registry.onFinish()

        assertEquals(1, firstFinishCount)
        assertEquals(1, secondFinishCount)
    }

    @Test
    fun `onFinish should invoke the remaining executors when one throws`() {
        var secondFinishCount = 0
        registry.registerExecutor("first", finishTrackingExecutor { error("boom") })
        registry.registerExecutor("second", finishTrackingExecutor { secondFinishCount++ })

        registry.onFinish()

        assertEquals(1, secondFinishCount)
        verify { logReporter.debug("onFinish failed for step type='first': boom") }
    }

    @Test
    fun `onFinish should be a no-op when no executors are registered`() {
        registry.onFinish()
    }

    private fun finishTrackingExecutor(onFinishAction: () -> Unit): StepExecutor =
        object : StepExecutor {
            override suspend fun execute(actionId: String, step: String): Result<StepResult> =
                Result.success(StepResult(outcome = Outcome.SUCCESS, actionId = actionId))

            override fun onFinish() = onFinishAction()
        }
}
