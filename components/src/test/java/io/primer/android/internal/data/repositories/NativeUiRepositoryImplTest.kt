package io.primer.android.internal.data.repositories

import android.content.Context
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManagerInterface
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference

class NativeUiRepositoryImplTest {

    private lateinit var mockContext: Context
    private lateinit var mockContextRef: WeakReference<Context>
    private lateinit var mockNativeUiManager: PrimerHeadlessUniversalCheckoutNativeUiManagerInterface
    private lateinit var repository: NativeUiRepositoryImpl

    @BeforeEach
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockContextRef = WeakReference(mockContext)
        mockNativeUiManager = mockk(relaxed = true)
        repository = NativeUiRepositoryImpl(mockContextRef)

        mockkObject(PrimerHeadlessUniversalCheckoutNativeUiManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(PrimerHeadlessUniversalCheckoutNativeUiManager)
    }

    @Test
    fun `startPaymentFlow creates new native UI manager and shows payment method`() {
        val paymentMethodType = "GOOGLE_PAY"
        val paymentMethodSlot = slot<String>()

        every {
            PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(
                paymentMethodType = capture(paymentMethodSlot),
            )
        } returns mockNativeUiManager

        justRun { mockNativeUiManager.showPaymentMethod(any(), any()) }

        repository.startPaymentFlow(paymentMethodType)

        verify(exactly = 1) {
            PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(
                paymentMethodType = paymentMethodType,
            )
        }
        verify(exactly = 1) {
            mockNativeUiManager.showPaymentMethod(mockContext, PrimerSessionIntent.CHECKOUT)
        }
    }

    @Test
    fun `startPaymentFlow cleans up existing manager before creating new one`() {
        val paymentMethodType = "GOOGLE_PAY"
        val firstManager = mockk<PrimerHeadlessUniversalCheckoutNativeUiManagerInterface>(relaxed = true)
        val secondManager = mockk<PrimerHeadlessUniversalCheckoutNativeUiManagerInterface>(relaxed = true)

        every {
            PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType = any())
        } returnsMany listOf(firstManager, secondManager)

        repository.startPaymentFlow(paymentMethodType)
        repository.startPaymentFlow(paymentMethodType)

        verify(exactly = 1) { firstManager.cleanup() }
        verify(exactly = 1) { firstManager.showPaymentMethod(mockContext, PrimerSessionIntent.CHECKOUT) }
        verify(exactly = 1) { secondManager.showPaymentMethod(mockContext, PrimerSessionIntent.CHECKOUT) }
    }

    @Test
    fun `startPaymentFlow does nothing when context is null`() {
        val emptyContextRef = WeakReference<Context>(null)
        val repository = NativeUiRepositoryImpl(emptyContextRef)

        repository.startPaymentFlow("GOOGLE_PAY")

        verify(exactly = 0) {
            PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType = any())
        }
    }

    @Test
    fun `cleanup cleans up native UI manager and sets it to null`() {
        val paymentMethodType = "GOOGLE_PAY"

        every {
            PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType = any())
        } returns mockNativeUiManager

        repository.startPaymentFlow(paymentMethodType)
        repository.cleanup()

        verify(exactly = 1) { mockNativeUiManager.cleanup() }
    }

    @Test
    fun `cleanup does nothing when manager is null`() {
        repository.cleanup()

        verify(exactly = 0) { mockNativeUiManager.cleanup() }
    }
}
