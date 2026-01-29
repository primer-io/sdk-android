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
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.data.repositories.HeadlessRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
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
        mockPrimerSettings = mockk {
            every { clientSessionCachingEnabled } returns false
        }
        mockPrimerConfig = createMockPrimerConfig(TEST_CLIENT_TOKEN)
        repository = HeadlessRepositoryImpl(mockHeadlessInterface, mockContextRef, mockPrimerConfig)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class AwaitPaymentResultTests {

        @Test
        fun `should set checkout listener when awaitPaymentResult is called`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            setupListenerToReturn { listener ->
                listener.onCheckoutCompleted(mockCheckoutData)
            }

            val deferred = async {
                repository.awaitPaymentResult()
            }

            advanceUntilIdle()

            // Verify listener was set
            verify(exactly = 1) {
                mockHeadlessInterface.setCheckoutListener(any())
            }

            // Complete the deferred to clean up
            deferred.await()
        }

        @Test
        fun `should return success when checkout completes`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val result = awaitPaymentResult { listener ->
                listener.onCheckoutCompleted(mockCheckoutData)
            }

            assertSuccessResult(result, mockCheckoutData)
        }

        @Test
        fun `should return failure when onFailed is called with error and checkout data`() = runTest {
            val mockError = createMockError(TEST_ERROR_MESSAGE)
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val result = awaitPaymentResult { listener ->
                listener.onFailed(mockError, mockCheckoutData)
            }

            assertFailureResult(result, TEST_ERROR_MESSAGE)
        }

        @Test
        fun `should return failure when onFailed is called with error only`() = runTest {
            val mockError = createMockError(TEST_ERROR_MESSAGE)

            val result = awaitPaymentResult { listener ->
                listener.onFailed(mockError)
            }

            assertFailureResult(result, TEST_ERROR_MESSAGE)
        }

        @Test
        fun `should ignore onAvailablePaymentMethodsLoaded and wait for payment result`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()

            val result = awaitPaymentResult { listener ->
                // This should not trigger completion
                listener.onAvailablePaymentMethodsLoaded(emptyList())
                // This should trigger completion
                listener.onCheckoutCompleted(mockCheckoutData)
            }

            assertSuccessResult(result, mockCheckoutData)
        }

        @Test
        fun `should only respond to first callback when multiple are called`() = runTest {
            val mockCheckoutData = mockk<PrimerCheckoutData>()
            val mockError = createMockError("Error")

            val result = awaitPaymentResult { listener ->
                // First callback should complete the suspension
                listener.onCheckoutCompleted(mockCheckoutData)
                // Second callback should be ignored (continuation already resumed)
                listener.onFailed(mockError)
            }

            // Should get success from first callback
            assertSuccessResult(result, mockCheckoutData)
        }
    }

    @Nested
    inner class GetAvailablePaymentMethodsTests {

        @Test
        fun `should return payment methods when loaded`() = runTest {
            val cardPaymentMethod = createMockPaymentMethod("PAYMENT_CARD")
            val nativeUiPaymentMethod = createMockPaymentMethod(
                "GOOGLE_PAY",
                listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            )
            val paymentMethodsFromHeadless = listOf(cardPaymentMethod, nativeUiPaymentMethod)

            setupHeadlessStartToReturn(paymentMethodsFromHeadless)

            val result = repository.getAvailablePaymentMethods()

            assertEquals(paymentMethodsFromHeadless, result)
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
            val expectedPaymentMethods = listOf(createMockPaymentMethod("PAYMENT_CARD"))
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

        @Test
        fun `should include PAYMENT_CARD type regardless of categories`() = runTest {
            val cardWithNoCategories = createMockPaymentMethod("PAYMENT_CARD", emptyList())
            val cardWithRawData = createMockPaymentMethod(
                "PAYMENT_CARD",
                listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            )

            setupHeadlessStartToReturn(listOf(cardWithNoCategories, cardWithRawData))

            val result = repository.getAvailablePaymentMethods()

            assertEquals(2, result.size)
            assertTrue(result.all { it.paymentMethodType == "PAYMENT_CARD" })
        }

        @Test
        fun `should include methods with NATIVE_UI category`() = runTest {
            val nativeUiMethod = createMockPaymentMethod(
                "GOOGLE_PAY",
                listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            )

            setupHeadlessStartToReturn(listOf(nativeUiMethod))

            val result = repository.getAvailablePaymentMethods()

            assertEquals(1, result.size)
            assertEquals("GOOGLE_PAY", result[0].paymentMethodType)
        }

        @Test
        fun `should include methods with KLARNA category`() = runTest {
            val klarnaMethod = createMockPaymentMethod(
                "KLARNA",
                listOf(PrimerPaymentMethodManagerCategory.KLARNA),
            )

            setupHeadlessStartToReturn(listOf(klarnaMethod))

            val result = repository.getAvailablePaymentMethods()

            assertEquals(1, result.size)
            assertEquals("KLARNA", result[0].paymentMethodType)
        }

        @Test
        fun `should filter out methods without supported categories`() = runTest {
            val rawDataMethod = createMockPaymentMethod(
                "CUSTOM_PM",
                listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            )
            val stripeAchMethod = createMockPaymentMethod(
                "STRIPE_ACH",
                listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH),
            )
            val nolPayMethod = createMockPaymentMethod(
                "NOL_PAY",
                listOf(PrimerPaymentMethodManagerCategory.NOL_PAY),
            )
            val componentWithRedirectMethod = createMockPaymentMethod(
                "REDIRECT_PM",
                listOf(PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT),
            )

            setupHeadlessStartToReturn(
                listOf(rawDataMethod, stripeAchMethod, nolPayMethod, componentWithRedirectMethod),
            )

            val result = repository.getAvailablePaymentMethods()

            assertTrue(result.isEmpty())
        }

        @Test
        fun `should filter out methods with no categories that are not PAYMENT_CARD`() = runTest {
            val unknownMethod = createMockPaymentMethod("UNKNOWN_PM", emptyList())

            setupHeadlessStartToReturn(listOf(unknownMethod))

            val result = repository.getAvailablePaymentMethods()

            assertTrue(result.isEmpty())
        }

        @Test
        fun `should correctly filter mixed payment methods list`() = runTest {
            val card = createMockPaymentMethod("PAYMENT_CARD", emptyList())
            val googlePay = createMockPaymentMethod(
                "GOOGLE_PAY",
                listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            )
            val klarna = createMockPaymentMethod(
                "KLARNA",
                listOf(PrimerPaymentMethodManagerCategory.KLARNA),
            )
            val rawDataMethod = createMockPaymentMethod(
                "RAW_DATA_PM",
                listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            )
            val stripeAch = createMockPaymentMethod(
                "STRIPE_ACH",
                listOf(PrimerPaymentMethodManagerCategory.STRIPE_ACH),
            )
            val unsupportedMethod = createMockPaymentMethod("UNSUPPORTED", emptyList())

            setupHeadlessStartToReturn(
                listOf(card, googlePay, klarna, rawDataMethod, stripeAch, unsupportedMethod),
            )

            val result = repository.getAvailablePaymentMethods()

            assertEquals(3, result.size)
            assertTrue(result.any { it.paymentMethodType == "PAYMENT_CARD" })
            assertTrue(result.any { it.paymentMethodType == "GOOGLE_PAY" })
            assertTrue(result.any { it.paymentMethodType == "KLARNA" })
        }

        @Test
        fun `should include method with multiple categories if one is supported`() = runTest {
            val multiCategoryMethod = createMockPaymentMethod(
                "MULTI_CATEGORY",
                listOf(
                    PrimerPaymentMethodManagerCategory.RAW_DATA,
                    PrimerPaymentMethodManagerCategory.NATIVE_UI,
                ),
            )

            setupHeadlessStartToReturn(listOf(multiCategoryMethod))

            val result = repository.getAvailablePaymentMethods()

            assertEquals(1, result.size)
            assertEquals("MULTI_CATEGORY", result[0].paymentMethodType)
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

    private fun setupListenerToReturn(
        action: (PrimerHeadlessUniversalCheckoutListener) -> Unit,
    ) {
        val listenerSlot = slot<PrimerHeadlessUniversalCheckoutListener>()
        every {
            mockHeadlessInterface.setCheckoutListener(capture(listenerSlot))
        } answers {
            action(listenerSlot.captured)
        }
    }

    private suspend fun awaitPaymentResult(
        action: (PrimerHeadlessUniversalCheckoutListener) -> Unit,
    ): Result<PrimerCheckoutData> {
        setupListenerToReturn(action)
        return repository.awaitPaymentResult()
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

    private fun createMockPaymentMethod(
        type: String,
        categories: List<PrimerPaymentMethodManagerCategory> = emptyList(),
    ): PrimerHeadlessUniversalCheckoutPaymentMethod = mockk {
        every { paymentMethodType } returns type
        every { paymentMethodName } returns "$type Name"
        every { supportedPrimerSessionIntents } returns emptyList()
        every { paymentMethodManagerCategories } returns categories
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
