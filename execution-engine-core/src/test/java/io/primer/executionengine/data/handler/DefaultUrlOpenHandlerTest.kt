package io.primer.executionengine.data.handler

import io.primer.android.core.InstantExecutorExtension
import io.primer.executionengine.domain.executor.ComponentResultEvent
import io.primer.executionengine.domain.handler.UrlOpenLaunchRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
internal class DefaultUrlOpenHandlerTest {

    private lateinit var handler: DefaultUrlOpenHandler

    @BeforeEach
    fun setUp() {
        handler = DefaultUrlOpenHandler()
    }

    @Test
    fun `launch should emit UrlOpenLaunchRequest with all parameters`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.launchRequest.first() }

        handler.launch("https://example.com", listOf("https://redirect.com"), "Pay Now")

        val request = deferred.await()
        assertEquals(
            UrlOpenLaunchRequest("https://example.com", listOf("https://redirect.com"), "Pay Now"),
            request,
        )
    }

    @Test
    fun `launch should emit request with null redirectUrls and title`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.launchRequest.first() }

        handler.launch("https://example.com", null, null)

        val request = deferred.await()
        assertEquals(UrlOpenLaunchRequest("https://example.com", null, null), request)
    }

    @Test
    fun `onResultOk should emit completed event`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.componentEvents.first() }

        handler.onResultOk()

        assertEquals(
            ComponentResultEvent(value = "completed", eventType = "custom"),
            deferred.await(),
        )
    }

    @Test
    fun `onResultCancelled should emit cancelled event`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.componentEvents.first() }

        handler.onResultCancelled()

        assertEquals(
            ComponentResultEvent(value = "cancelled", eventType = "custom"),
            deferred.await(),
        )
    }

    @Test
    fun `onResultError should emit error event`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.componentEvents.first() }

        handler.onResultError("https://error.example.com")

        assertEquals(
            ComponentResultEvent(value = "error", eventType = "custom"),
            deferred.await(),
        )
    }
}
