@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.executionengine.data.handler

import io.primer.android.core.InstantExecutorExtension
import io.primer.executionengine.domain.executor.ComponentResultEvent
import io.primer.executionengine.domain.handler.UrlCloseReason
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

        handler.launch("https://example.com", listOf("https://redirect.com"))

        val request = deferred.await()
        assertEquals(
            UrlOpenLaunchRequest("https://example.com", listOf("https://redirect.com")),
            request,
        )
    }

    @Test
    fun `launch should emit request with null redirectUrls`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.launchRequest.first() }

        handler.launch("https://example.com", null)

        val request = deferred.await()
        assertEquals(UrlOpenLaunchRequest("https://example.com", null), request)
    }

    @Test
    fun `onClosed with AUTO should emit cancelled event with closeReason auto`() =
        runTest(UnconfinedTestDispatcher()) {
            val deferred = async { handler.componentEvents.first() }

            handler.onClosed(UrlCloseReason.AUTO)

            assertEquals(
                ComponentResultEvent(
                    value = "cancelled",
                    eventType = "custom",
                    data = mapOf("closeReason" to "auto"),
                ),
                deferred.await(),
            )
        }

    @Test
    fun `onClosed with USER should emit cancelled event with closeReason user`() =
        runTest(UnconfinedTestDispatcher()) {
            val deferred = async { handler.componentEvents.first() }

            handler.onClosed(UrlCloseReason.USER)

            assertEquals(
                ComponentResultEvent(
                    value = "cancelled",
                    eventType = "custom",
                    data = mapOf("closeReason" to "user"),
                ),
                deferred.await(),
            )
        }

    @Test
    fun `onResultError should emit error event without data`() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async { handler.componentEvents.first() }

        handler.onResultError("https://error.example.com")

        assertEquals(
            ComponentResultEvent(value = "error", eventType = "custom", data = emptyMap()),
            deferred.await(),
        )
    }
}
