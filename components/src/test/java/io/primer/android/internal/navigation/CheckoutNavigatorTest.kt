package io.primer.android.internal.navigation

import io.mockk.mockk
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutNavigatorTest {

    private lateinit var navigator: CheckoutNavigator

    @BeforeEach
    fun setUp() {
        navigator = CheckoutNavigator()
    }

    @Test
    fun `navigateToCardForm emits NavigateToCardForm event`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.navigateToCardForm()

        assertEquals(1, events.size)
        assertTrue(events[0] is CheckoutNavigator.NavigationEvent.NavigateToCardForm)

        job.cancel()
    }

    @Test
    fun `navigateToVaultManage emits NavigateToVaultManage event`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.navigateToVaultManage()

        assertEquals(1, events.size)
        assertTrue(events[0] is CheckoutNavigator.NavigationEvent.NavigateToVaultManage)

        job.cancel()
    }

    @Test
    fun `showCvvRecapture emits ShowCvvRecapture event with vaulted method`() = runTest(UnconfinedTestDispatcher()) {
        val mockVaultedMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.showCvvRecapture(mockVaultedMethod)

        assertEquals(1, events.size)
        val event = events[0] as CheckoutNavigator.NavigationEvent.ShowCvvRecapture
        assertEquals(mockVaultedMethod, event.vaultedMethod)

        job.cancel()
    }

    @Test
    fun `startPaymentFlow emits StartPaymentFlow event with payment method type`() = runTest(UnconfinedTestDispatcher()) {
        val paymentMethodType = "PAYMENT_CARD"
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.startPaymentFlow(paymentMethodType)

        assertEquals(1, events.size)
        val event = events[0] as CheckoutNavigator.NavigationEvent.StartPaymentFlow
        assertEquals(paymentMethodType, event.paymentMethodType)

        job.cancel()
    }

    @Test
    fun `startVaultedPaymentFlow emits StartVaultedPaymentFlow event with vaulted method`() = runTest(UnconfinedTestDispatcher()) {
        val mockVaultedMethod = mockk<PrimerVaultedPaymentMethod>(relaxed = true)
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.startVaultedPaymentFlow(mockVaultedMethod)

        assertEquals(1, events.size)
        val event = events[0] as CheckoutNavigator.NavigationEvent.StartVaultedPaymentFlow
        assertEquals(mockVaultedMethod, event.vaultedMethod)

        job.cancel()
    }

    @Test
    fun `navigateToLoading emits NavigateToLoading event`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.navigateToLoading()

        assertEquals(1, events.size)
        assertTrue(events[0] is CheckoutNavigator.NavigationEvent.NavigateToLoading)

        job.cancel()
    }

    @Test
    fun `dismiss emits Dismiss event`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.dismiss()

        assertEquals(1, events.size)
        assertTrue(events[0] is CheckoutNavigator.NavigationEvent.Dismiss)

        job.cancel()
    }

    @Test
    fun `onSuccess emits NavigateToSuccess event with checkout data`() = runTest(UnconfinedTestDispatcher()) {
        val mockCheckoutData = mockk<PrimerCheckoutData>(relaxed = true)
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.onSuccess(mockCheckoutData)

        assertEquals(1, events.size)
        val event = events[0] as CheckoutNavigator.NavigationEvent.NavigateToSuccess
        assertEquals(mockCheckoutData, event.checkoutData)

        job.cancel()
    }

    @Test
    fun `onError emits NavigateToError event with error`() = runTest(UnconfinedTestDispatcher()) {
        val mockError = mockk<PrimerError>(relaxed = true)
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.onError(mockError)

        assertEquals(1, events.size)
        val event = events[0] as CheckoutNavigator.NavigationEvent.NavigateToError
        assertEquals(mockError, event.error)

        job.cancel()
    }

    @Test
    fun `signalDismissAfterResult emits DismissAfterResult event`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.signalDismissAfterResult()

        assertEquals(1, events.size)
        assertTrue(events[0] is CheckoutNavigator.NavigationEvent.DismissAfterResult)

        job.cancel()
    }

    @Test
    fun `multiple navigation calls emit events in order`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<CheckoutNavigator.NavigationEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.navigationEvents.collect { events.add(it) }
        }

        navigator.navigateToCardForm()
        navigator.navigateToLoading()
        navigator.dismiss()

        assertEquals(3, events.size)
        assertTrue(events[0] is CheckoutNavigator.NavigationEvent.NavigateToCardForm)
        assertTrue(events[1] is CheckoutNavigator.NavigationEvent.NavigateToLoading)
        assertTrue(events[2] is CheckoutNavigator.NavigationEvent.Dismiss)

        job.cancel()
    }

    @Test
    fun `SUCCESS_SCREEN_DELAY_MS constant has expected value`() {
        assertEquals(3000L, CheckoutNavigator.SUCCESS_SCREEN_DELAY_MS)
    }
}
