package io.primer.composable.internal.data.repositories

import android.content.Context
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.components.PrimerHeadlessUniversalCheckoutInterface
import io.primer.android.components.PrimerHeadlessUniversalCheckoutListener
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.data.repositories.HeadlessRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.ref.WeakReference

@OptIn(ExperimentalCoroutinesApi::class)
class HeadlessRepositoryImplTest {

    private lateinit var mockHeadlessInterface: PrimerHeadlessUniversalCheckoutInterface
    private lateinit var repository: HeadlessRepositoryImpl

    private lateinit var mockContext: Context
    private lateinit var mockContextRef: WeakReference<Context>
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockPrimerSettings: PrimerSettings

    companion object {
        private const val TEST_CLIENT_TOKEN = "test-client-token"
        private const val TEST_ERROR_MESSAGE = "Test error"
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockHeadlessInterface = mockk(relaxed = true)
        mockContext = mockk()
        mockContextRef = WeakReference(mockContext)
        mockPrimerSettings = mockk()
        mockPrimerConfig = createMockPrimerConfig(TEST_CLIENT_TOKEN)
        repository = HeadlessRepositoryImpl(mockHeadlessInterface, mockContextRef, mockPrimerConfig)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class PaymentResultsTests {

        @Test
        fun `should set checkout listener when flow is collected`() = runTest {
            val listenerSlot = captureCheckoutListener()

            // Start collecting from the flow
            val job = launch {
                repository.paymentResults.take(1).toList()
            }

            advanceUntilIdle()

            // Verify listener was set
            verify(exactly = 1) {
                mockHeadlessInterface.setCheckoutListener(any())
            }

            assertTrue(listenerSlot.isCaptured)

            job.cancel()
        }

        @Test
        fun `should emit success when checkout completes`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val results = collectPaymentResults(1) { listener ->
                listener.onCheckoutCompleted(mockCheckoutData)
            }

            assertSuccessResult(results.single(), mockCheckoutData)
        }

        @Test
        fun `should emit failure when onFailed is called with error and checkout data`() = runTest {
            val mockError = createMockError(TEST_ERROR_MESSAGE)
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val results = collectPaymentResults(1) { listener ->
                listener.onFailed(mockError, mockCheckoutData)
            }

            assertFailureResult(results.single(), TEST_ERROR_MESSAGE)
        }

        @Test
        fun `should emit failure when onFailed is called with error only`() = runTest {
            val mockError = createMockError(TEST_ERROR_MESSAGE)

            val results = collectPaymentResults(1) { listener ->
                listener.onFailed(mockError)
            }

            assertFailureResult(results.single(), TEST_ERROR_MESSAGE)
        }

        @Test
        fun `should test both onFailed overloads separately`() = runTest {
            val mockError = createMockError("Different error")
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val results = collectPaymentResults(2) { listener ->
                // Test overload with checkoutData parameter
                listener.onFailed(mockError, mockCheckoutData)
                // Test overload without checkoutData parameter
                listener.onFailed(mockError)
            }

            assertEquals(2, results.size)
            assertFailureResult(results[0], "Different error")
            assertFailureResult(results[1], "Different error")
        }

        @Test
        fun `should handle multiple emissions in sequence`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()
            val mockError = createMockError("Error")

            val results = collectPaymentResults(2) { listener ->
                listener.onFailed(mockError)
                listener.onCheckoutCompleted(mockCheckoutData)
            }

            assertEquals(2, results.size)
            assertFailureResult(results[0], "Error")
            assertSuccessResult(results[1], mockCheckoutData)
        }

        @Test
        fun `should not emit when onAvailablePaymentMethodsLoaded is called`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val results = collectPaymentResults(1) { listener ->
                // This should not emit
                listener.onAvailablePaymentMethodsLoaded(emptyList())
                // This should emit
                listener.onCheckoutCompleted(mockCheckoutData)
            }

            // Only one result from onCheckoutCompleted
            assertEquals(1, results.size)
            assertSuccessResult(results.single(), mockCheckoutData)
        }
    }

    @Nested
    inner class GetAvailablePaymentMethodsTests {

        @Test
        fun `should return payment methods when loaded`() = runTest {
            val expectedPaymentMethods = listOf(
                createMockPaymentMethod("PAYMENT_CARD"),
                createMockPaymentMethod("GOOGLE_PAY"),
            )

            setupHeadlessStartToReturn(expectedPaymentMethods)

            val result = repository.getAvailablePaymentMethods()

            assertEquals(expectedPaymentMethods, result)
            verify {
                mockHeadlessInterface.start(
                    context = mockContext,
                    clientToken = TEST_CLIENT_TOKEN,
                    settings = mockPrimerSettings,
                    checkoutListener = any(),
                    uiListener = null,
                )
            }
        }

        @Test
        fun `should handle empty payment methods list`() = runTest {
            setupHeadlessStartToReturn(emptyList())

            val result = repository.getAvailablePaymentMethods()

            assertNotNull(result)
            assertTrue(result.isEmpty())
        }

        @Test
        fun `should throw NullPointerException when client token is null`() = runTest {
            val mockConfigWithNullToken = createMockPrimerConfig(clientToken = null)
            val repoWithNullToken = HeadlessRepositoryImpl(
                mockHeadlessInterface,
                mockContextRef,
                mockConfigWithNullToken,
            )

            assertThrows<NullPointerException> {
                repoWithNullToken.getAvailablePaymentMethods()
            }
        }

        @Test
        fun `should only respond to onAvailablePaymentMethodsLoaded callback`() = runTest {
            val expectedPaymentMethods = listOf(createMockPaymentMethod("TEST"))
            val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()

            every {
                mockHeadlessInterface.start(
                    any(),
                    any(),
                    any(),
                    capture(listenerSlot),
                    any(),
                )
            } answers {
                // Try calling onCheckoutCompleted first - should be ignored
                listenerSlot.captured.onCheckoutCompleted(mockk())
                // Then call the correct callback
                listenerSlot.captured.onAvailablePaymentMethodsLoaded(expectedPaymentMethods)
            }

            val result = repository.getAvailablePaymentMethods()

            assertEquals(expectedPaymentMethods, result)
        }
    }

    // Helper methods
    private fun captureCheckoutListener(): CapturingSlot<PrimerHeadlessUniversalCheckoutListener> {
        val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
        every {
            mockHeadlessInterface.setCheckoutListener(capture(listenerSlot))
        } answers { }
        return listenerSlot
    }

    private suspend fun TestScope.collectPaymentResults(
        count: Int,
        action: (PrimerHeadlessUniversalCheckoutListener) -> Unit,
    ): List<Result<PrimerCheckoutData>> {
        val listenerSlot = captureCheckoutListener()
        val results = mutableListOf<Result<PrimerCheckoutData>>()

        val job = launch {
            repository.paymentResults.take(count).toList(results)
        }

        advanceUntilIdle()
        assertTrue(listenerSlot.isCaptured)
        action(listenerSlot.captured)
        advanceUntilIdle()

        job.join()
        return results
    }

    private fun setupHeadlessStartToReturn(paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
        every {
            mockHeadlessInterface.start(
                any(),
                any(),
                any(),
                capture(listenerSlot),
                any(),
            )
        } answers {
            listenerSlot.captured.onAvailablePaymentMethodsLoaded(paymentMethods)
        }
    }

    private fun createMockPrimerConfig(clientToken: String?): PrimerConfig = mockk {
        every { clientTokenBase64 } returns clientToken
        every { settings } returns mockPrimerSettings
    }

    private fun createMockError(description: String): PrimerError = mockk {
        every { this@mockk.description } returns description
    }

    private fun createMockPaymentMethod(type: String): PrimerHeadlessUniversalCheckoutPaymentMethod = mockk {
        every { paymentMethodType } returns type
        every { paymentMethodName } returns "$type Name"
        every { supportedPrimerSessionIntents } returns emptyList()
        every { paymentMethodManagerCategories } returns emptyList()
        every { requiredInputDataClass } returns null
    }

    private fun assertSuccessResult(result: Result<PrimerCheckoutData>, expected: PrimerCheckoutData) {
        assertTrue(result.isSuccess, "Expected success result but was failure: ${result.exceptionOrNull()}")
        assertEquals(expected, result.getOrNull())
    }

    private fun assertFailureResult(result: Result<PrimerCheckoutData>, expectedMessage: String) {
        assertTrue(result.isFailure, "Expected failure result but was success")
        assertEquals(expectedMessage, result.exceptionOrNull()?.message)
    }
}
