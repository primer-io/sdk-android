package io.primer.checkout.orchestrator.domain

import android.util.Base64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.core.domain.None
import io.primer.android.core.logging.internal.LogReporter
import io.primer.checkout.orchestrator.data.StateProcessorLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PreloadStateProcessorInteractorTest {

    @MockK
    lateinit var stateProcessorLoader: StateProcessorLoader

    private val logReporter = mockk<LogReporter>(relaxed = true)
    private lateinit var interactor: PreloadStateProcessorInteractor

    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        coEvery { stateProcessorLoader.load(any()) } returns mockk()
        interactor = PreloadStateProcessorInteractor(
            stateProcessorLoader = stateProcessorLoader,
            clientTokenProvider = { CLIENT_TOKEN },
            logReporter = logReporter,
            dispatcher = Dispatchers.Unconfined,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test
    fun `loads the client token's environment`() = runTest {
        givenTokenPayload("""{"env":"SANDBOX"}""")

        assertTrue(interactor(None).isSuccess)

        coVerify { stateProcessorLoader.load("sandbox") }
    }

    @Test
    fun `loads nothing for a token without an environment`() = runTest {
        givenTokenPayload("{}")

        assertTrue(interactor(None).isSuccess)

        coVerify(exactly = 0) { stateProcessorLoader.load(any()) }
    }

    @Test
    fun `logs a failed preload`() = runTest {
        givenTokenPayload("""{"env":"SANDBOX"}""")
        coEvery { stateProcessorLoader.load(any()) } throws IllegalStateException("offline")

        assertTrue(interactor(None).isFailure)

        verify { logReporter.debug(match { it.contains("preload failed") && it.contains("offline") }) }
    }

    private fun givenTokenPayload(payload: String) {
        every { Base64.decode("payload", any<Int>()) } returns payload.toByteArray()
    }

    private companion object {
        const val CLIENT_TOKEN = "header.payload.signature"
    }
}
