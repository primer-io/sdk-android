package io.primer.jscore.infrastructure.core

import androidx.javascriptengine.EvaluationFailedException
import androidx.javascriptengine.IsolateTerminatedException
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import androidx.javascriptengine.SandboxDeadException
import com.google.common.util.concurrent.Futures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class JsSandboxExecutorTest {

    private val sandbox = mockk<JavaScriptSandbox>(relaxed = true)
    private val isolate = mockk<JavaScriptIsolate>(relaxed = true)
    private val sandboxProvider = mockk<JavaScriptSandboxProvider>(relaxed = true)
    private val executor = JsSandboxExecutor(sandboxProvider, mockk(relaxed = true), mockk())

    @BeforeEach
    fun setUp() {
        coEvery { sandboxProvider.get() } returns sandbox
        every { sandbox.createIsolate() } returns isolate
        every { sandbox.isFeatureSupported(any()) } returns false
        every { isolate.evaluateJavaScriptAsync(any<String>()) } returns Futures.immediateFuture("{}")
    }

    @Test
    fun `is alive once initialized`() = runTest {
        assertFalse(executor.isAlive)

        executor.initialize(emptyList()).getOrThrow()

        assertTrue(executor.isAlive)
    }

    @Test
    fun `is no longer alive once its isolate is terminated`() = runTest {
        executor.initialize(emptyList()).getOrThrow()
        givenEvaluationsFail(IsolateTerminatedException("memory limit exceeded"))

        assertTrue(executor.checkClientRequirements("[]", "{}").isFailure)

        assertFalse(executor.isAlive)
        coVerify(exactly = 0) { sandboxProvider.drop(any()) }
    }

    @Test
    fun `drops a sandbox that died, so the next initialize connects a new one`() = runTest {
        executor.initialize(emptyList()).getOrThrow()
        givenEvaluationsFail(SandboxDeadException("sandbox dead"))

        assertTrue(executor.checkClientRequirements("[]", "{}").isFailure)

        assertFalse(executor.isAlive)
        coVerify { sandboxProvider.drop(sandbox) }
    }

    @Test
    fun `stays alive when a script fails`() = runTest {
        executor.initialize(emptyList()).getOrThrow()
        givenEvaluationsFail(EvaluationFailedException("TypeError"))

        assertTrue(executor.checkClientRequirements("[]", "{}").isFailure)

        assertTrue(executor.isAlive)
    }

    private fun givenEvaluationsFail(cause: Exception) {
        every { isolate.evaluateJavaScriptAsync(any<String>()) } returns Futures.immediateFailedFuture(cause)
    }
}
