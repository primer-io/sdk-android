@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.core.data.network

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import kotlin.random.Random.Default.nextInt
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PrimerHttpClientTest {
    @Test
    fun `withTimeout should return the same instance for the same timeout`() {
        val client = createPrimerHttpClient()
        val timeout = 30.seconds

        val instance1 = client.withTimeout(timeout)
        val instance2 = client.withTimeout(timeout)

        assertSame(
            expected = instance1,
            actual = instance2,
            message = "Instances should be the same for the same timeout",
        )
    }

    @Test
    fun `withTimeout should create a new instance for different timeouts`() {
        val client = createPrimerHttpClient()

        val timeout1 = 30.seconds
        val timeout2 = 60.seconds

        val instance1 = client.withTimeout(timeout1)
        val instance2 = client.withTimeout(timeout2)

        assertNotSame(instance1, instance2, "Instances should be different for different timeouts")
    }

    @Test
    fun `withTimeout should not affect the original client`() {
        val client = createPrimerHttpClient()
        val timeout = 30.seconds

        val newClient = client.withTimeout(timeout)

        assertNotSame(client, newClient, "withTimeout should not modify the original instance")
    }

    @Test
    fun `withTimeout should correctly configure timeout settings`() {
        val client = createPrimerHttpClient()
        val timeout = 30.seconds

        val newClient = client.withTimeout(timeout)

        val okHttpClient = newClient.okHttpClient
        assertEquals(
            expected = timeout,
            actual = okHttpClient.readTimeoutMillis.toLong().milliseconds,
            message = "Read timeout mismatch",
        )
        assertEquals(
            expected = timeout,
            actual = okHttpClient.writeTimeoutMillis.toLong().milliseconds,
            message = "Write timeout mismatch",
        )
    }

    @Test
    fun `cleanup should clear cached instances`() {
        val client = createPrimerHttpClient()
        val timeout = 30.seconds

        val instance = client.withTimeout(timeout)
        PrimerHttpClient.clearCustomTimeoutInstances()

        val newInstance = client.withTimeout(timeout)
        assertNotSame(instance, newInstance, "Instances should be different after cleanup")
    }

    @Test
    fun `withTimeout should work with multiple durations`() {
        val client = createPrimerHttpClient()
        val timeouts = listOf(10.seconds, 20.seconds, 30.seconds)

        val instances = timeouts.map { client.withTimeout(it) }

        assertEquals(
            expected = instances.size,
            actual = timeouts.size,
            message = "Each timeout should produce a unique instance",
        )
    }

    @Test
    fun `withTimeout should handle concurrent calls`() =
        runTest {
            val client = createPrimerHttpClient()
            val timeout = 30.seconds

            val instances =
                (1..10).map {
                    async(newSingleThreadContext(nextInt().toString())) { client.withTimeout(timeout) }
                }.map { it.await() }

            assertEquals(
                1,
                instances.toSet().size,
                "Concurrent calls with the same timeout should return the same instance",
            )
        }

    private fun createPrimerHttpClient(): PrimerHttpClient {
        return PrimerHttpClient(
            okHttpClient = OkHttpClient().newBuilder().build(),
            logProvider = mockk(),
            messagePropertiesEventProvider = mockk(),
        )
    }
}
