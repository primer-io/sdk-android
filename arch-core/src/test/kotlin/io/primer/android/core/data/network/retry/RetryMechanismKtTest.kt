package io.primer.android.core.data.network.retry

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.primer.android.core.data.network.helpers.MessageLog
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertContains

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class RetryMechanismKtTest {
    @RelaxedMockK
    internal lateinit var logger: EventFlowProvider<MessageLog>

    @RelaxedMockK
    internal lateinit var response: Response

    @RelaxedMockK
    internal lateinit var messagePropertiesEventProvider: EventFlowProvider<MessagePropertiesHelper>

    private val config =
        RetryConfig(
            enabled = true,
            retryNetworkErrors = true,
            retry500Errors = true,
        )

    @Test
    fun `should not retry on successful response`() =
        runTest {
            every { response.isSuccessful } returns true

            val result = retry(response, config, logger, messagePropertiesEventProvider)

            assertFalse(result)
        }

    @Test
    fun `should not retry when config is disabled`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = false,
                )
            every { response.isSuccessful } returns false
            every { response.code } returns 500

            val result = retry(response, config, logger, messagePropertiesEventProvider)

            assertFalse(result)
        }

    @Test
    fun `should not retry on network error when retryNetworkErrors is false`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = true,
                    retryNetworkErrors = false,
                )
            every { response.isSuccessful } returns false
            every { response.code } returns NETWORK_EXCEPTION_ERROR_CODE

            val result = retry(response, config, logger, messagePropertiesEventProvider)

            assertFalse(result)
        }

    @Test
    fun `should retry on network error when retryNetworkErrors is true`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = true,
                    retryNetworkErrors = true,
                )
            every { response.isSuccessful } returns false
            every { response.code } returns NETWORK_EXCEPTION_ERROR_CODE

            val result = retry(response, config, logger, messagePropertiesEventProvider)

            assertTrue(result)
        }

    @Test
    fun `should not retry on 400 error`() =
        runTest {
            every { response.code } returns 400

            val result = retry(response, config, logger, messagePropertiesEventProvider)

            assertFalse(result)
        }

    @Test
    fun `retry should stop after maximum retries`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = true,
                    retryNetworkErrors = true,
                    retry500Errors = true,
                    maxRetries = 3,
                )

            every { response.isSuccessful } returns false
            every { response.code } returns 500

            repeat(4) { config.retries++ }

            val result = retry(response, config, logger, messagePropertiesEventProvider)

            assertFalse(result)
        }

    @Test
    fun `logger should log retry attempt`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = true,
                    retryNetworkErrors = true,
                    retry500Errors = true,
                )

            every { response.isSuccessful } returns false
            every { response.code } returns 500

            retry(response, config, logger, messagePropertiesEventProvider)

            coVerify { logger.getEventProvider().emit(any()) }
        }

    @Test
    fun `logger should log network error`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = true,
                    retryNetworkErrors = true,
                )

            every { response.isSuccessful } returns false
            every { response.code } returns NETWORK_EXCEPTION_ERROR_CODE

            retry(response, config, logger, messagePropertiesEventProvider)

            val slot = slot<MessageLog>()

            coVerify { logger.getEventProvider().emit(capture(slot)) }

            assertContains(slot.captured.message, "Network error encountered")
        }

    @Test
    fun `logger should log server error`() =
        runTest {
            val config =
                RetryConfig(
                    enabled = true,
                    retry500Errors = true,
                )

            every { response.isSuccessful } returns false
            every { response.code } returns 500

            retry(response, config, logger, messagePropertiesEventProvider)

            val slot = slot<MessageLog>()

            coVerify { logger.getEventProvider().emit(capture(slot)) }

            assertContains(slot.captured.message, "HTTP 500 error encountered")
        }

    @Test
    fun `networkError should return response with correct code`() {
        val response = networkError(DUMMY_URL)

        assertEquals(NETWORK_EXCEPTION_ERROR_CODE, response.code)
    }

    @Test
    fun `networkError should return response with correct protocol`() {
        val response = networkError(DUMMY_URL)

        assertEquals(Protocol.HTTP_2, response.protocol)
    }

    @Test
    fun `networkError should return response with correct body`() {
        val response = networkError(DUMMY_URL)

        val expectedBody =
            """
            {
                "description":"Network error encountered when retrying"
            }
            """.trimIndent()

        assertEquals(expectedBody, response.body?.string())
    }

    @Test
    fun `networkError should return response with non-null body`() {
        val response = networkError(DUMMY_URL)

        assertNotNull(response.body)
    }

    @Test
    fun `networkError should return response with correct content type`() {
        val response = networkError(DUMMY_URL)

        assertEquals("application/json; charset=utf-8", response.body?.contentType().toString())
    }

    companion object {
        private const val DUMMY_URL = "http://www.test.com"
    }
}
