package io.primer.executionengine.data.executors.urlopen

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.primer.android.core.InstantExecutorExtension
import io.primer.executionengine.domain.executor.ComponentResultEvent
import io.primer.executionengine.domain.handler.UrlOpenHandler
import io.primer.executionengine.domain.models.Outcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class UrlOpenStepExecutorTest {

    @MockK
    lateinit var urlOpenHandler: UrlOpenHandler

    private val componentEvents = MutableSharedFlow<ComponentResultEvent>(replay = 1)

    private lateinit var executor: UrlOpenStepExecutor

    @BeforeEach
    fun setUp() {
        every { urlOpenHandler.componentEvents } returns componentEvents
        executor = UrlOpenStepExecutor(urlOpenHandler)
    }

    @Test
    fun `execute should return SUCCESS when event value is completed`() = runTest {
        val step = JSONObject().apply {
            put("url", "https://example.com")
        }.toString()

        every { urlOpenHandler.launch("https://example.com", null) } answers {
            componentEvents.tryEmit(ComponentResultEvent(value = "completed", eventType = "custom"))
        }

        val result = executor.execute("action-1", step)

        assertTrue(result.isSuccess)
        val stepResult = result.getOrThrow()
        assertEquals(Outcome.SUCCESS, stepResult.outcome)
        assertEquals("action-1", stepResult.actionId)
        assertEquals(emptyMap<String, Any?>(), stepResult.data)
    }

    @Test
    fun `execute should return CANCELLED when event value is cancelled`() = runTest {
        val step = JSONObject().apply {
            put("url", "https://example.com")
        }.toString()

        every { urlOpenHandler.launch(any(), any()) } answers {
            componentEvents.tryEmit(ComponentResultEvent(value = "cancelled", eventType = "custom"))
        }

        val result = executor.execute("action-1", step)

        assertTrue(result.isSuccess)
        assertEquals(Outcome.CANCELLED, result.getOrThrow().outcome)
    }

    @Test
    fun `execute should return ERROR when event value is unknown`() = runTest {
        val step = JSONObject().apply {
            put("url", "https://example.com")
        }.toString()

        every { urlOpenHandler.launch(any(), any()) } answers {
            componentEvents.tryEmit(ComponentResultEvent(value = "something_else", eventType = "custom"))
        }

        val result = executor.execute("action-1", step)

        assertTrue(result.isSuccess)
        assertEquals(Outcome.ERROR, result.getOrThrow().outcome)
    }

    @Test
    fun `execute should pass redirectUrls and webview title to handler`() = runTest {
        val step = JSONObject().apply {
            put("url", "https://pay.example.com")
            put("redirectUrls", org.json.JSONArray(listOf("https://return.example.com")))
            put("webview", JSONObject().put("title", "Payment"))
        }.toString()

        every { urlOpenHandler.launch(any(), any()) } answers {
            componentEvents.tryEmit(ComponentResultEvent(value = "completed", eventType = "custom"))
        }

        executor.execute("action-1", step)

        verify {
            urlOpenHandler.launch(
                "https://pay.example.com",
                listOf("https://return.example.com"),
            )
        }
    }

    @Test
    fun `execute should return failure when step JSON is invalid`() = runTest {
        val result = executor.execute("action-1", "not valid json")

        assertTrue(result.isFailure)
    }

    @Test
    fun `execute should return failure when required url field is missing`() = runTest {
        val step = JSONObject().apply {
            put("other", "value")
        }.toString()

        val result = executor.execute("action-1", step)

        assertTrue(result.isFailure)
    }
}
