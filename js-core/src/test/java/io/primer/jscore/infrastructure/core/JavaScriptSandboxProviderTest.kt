package io.primer.jscore.infrastructure.core

import android.content.Context
import androidx.javascriptengine.JavaScriptSandbox
import com.google.common.util.concurrent.Futures
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class JavaScriptSandboxProviderTest {

    private val first = mockk<JavaScriptSandbox>(relaxed = true)
    private val second = mockk<JavaScriptSandbox>(relaxed = true)
    private val provider = JavaScriptSandboxProvider(mockk<Context>())

    @BeforeEach
    fun setUp() {
        mockkStatic(JavaScriptSandbox::class)
        every { JavaScriptSandbox.isSupported() } returns true
        every { JavaScriptSandbox.createConnectedInstanceAsync(any()) } returnsMany
            listOf(Futures.immediateFuture(first), Futures.immediateFuture(second))
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(JavaScriptSandbox::class)
    }

    @Test
    fun `connects once, for every caller`() = runTest {
        assertSame(first, provider.get())
        assertSame(first, provider.get())
    }

    @Test
    fun `connects a new sandbox once the current one died`() = runTest {
        provider.get()

        provider.drop(first)

        assertSame(second, provider.get())
        verify { first.close() }
    }

    @Test
    fun `keeps its sandbox when an earlier one is dropped`() = runTest {
        provider.get()

        provider.drop(second)

        assertSame(first, provider.get())
    }
}
