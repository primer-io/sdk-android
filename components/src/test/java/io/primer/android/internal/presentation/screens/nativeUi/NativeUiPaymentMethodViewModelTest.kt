package io.primer.android.internal.presentation.screens.nativeUi

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NativeUiPaymentMethodViewModelTest {

    private lateinit var mockStartNativeUiPaymentUseCase: StartNativeUiPaymentUseCase
    private lateinit var logReporter: LogReporter
    private lateinit var viewModel: NativeUiPaymentMethodViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val paymentMethodType = "GOOGLE_PAY"

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockStartNativeUiPaymentUseCase = mockk(relaxed = true)
        logReporter = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init starts payment flow and emits PaymentSuccess on successful payment`() = runTest {
        val mockCheckoutData = mockk<PrimerCheckoutData>()

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockCheckoutData)

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            logReporter = logReporter,
        )

        advanceUntilIdle()

        val event = viewModel.navigation.first()
        assertTrue(event is NativeUiPaymentMethodViewModel.NavigationEvent.PaymentSuccess)
        assertEquals(
            mockCheckoutData,
            (event as NativeUiPaymentMethodViewModel.NavigationEvent.PaymentSuccess).checkoutData,
        )

        val state = viewModel.state.value
        assertFalse(state.isProcessing)
        assertNull(state.error)

        coVerify(exactly = 1) { mockStartNativeUiPaymentUseCase(paymentMethodType) }
    }

    @Test
    fun `init starts payment flow and emits PaymentError on failed payment`() = runTest {
        val errorMessage = "Payment failed"
        val error = RuntimeException(errorMessage)

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.failure(error)

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            logReporter = logReporter,
        )

        advanceUntilIdle()

        val event = viewModel.navigation.first()
        assertTrue(event is NativeUiPaymentMethodViewModel.NavigationEvent.PaymentError)
        assertTrue(
            (event as NativeUiPaymentMethodViewModel.NavigationEvent.PaymentError).error.description.contains(
                errorMessage,
            ),
        )

        val state = viewModel.state.value
        assertFalse(state.isProcessing)
        assertTrue(state.error?.contains(errorMessage) == true)

        coVerify(exactly = 1) { mockStartNativeUiPaymentUseCase(paymentMethodType) }
    }

    @Test
    fun `state is initially processing when payment starts`() = runTest {
        val mockCheckoutData = mockk<PrimerCheckoutData>()

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(mockCheckoutData)
        }

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            logReporter = logReporter,
        )

        val initialState = viewModel.state.value
        assertTrue(initialState.isProcessing)
        assertNull(initialState.error)
    }

    @Test
    fun `onCleared cleans up payment use case`() = runTest {
        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockk())
        justRun { mockStartNativeUiPaymentUseCase.cleanup() }

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            logReporter = logReporter,
        )

        val onClearedMethod = viewModel::class.java.getDeclaredMethod("onCleared")
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        verify(exactly = 1) { mockStartNativeUiPaymentUseCase.cleanup() }
    }

    @Test
    fun `error with null message uses fallback message in state and event`() = runTest {
        val error = RuntimeException()
        val fallbackMessage = "Payment failed"

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.failure(error)

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            logReporter = logReporter,
        )

        advanceUntilIdle()

        val event = viewModel.navigation.first()
        assertTrue(event is NativeUiPaymentMethodViewModel.NavigationEvent.PaymentError)
        assertTrue(
            (event as NativeUiPaymentMethodViewModel.NavigationEvent.PaymentError).error.description.contains(
                fallbackMessage,
            ),
        )

        val state = viewModel.state.value
        assertFalse(state.isProcessing)
        assertTrue(state.error?.contains(fallbackMessage) == true)
    }

    @Test
    fun `state flow is properly exposed as StateFlow`() = runTest {
        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockk())

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            logReporter = logReporter,
        )

        assertNotNull(viewModel.state)
        val initialState = viewModel.state.value
        assertNotNull(initialState)
    }

    @Test
    fun `different payment method types are passed correctly`() = runTest {
        val paymentTypes = listOf("PAYPAL", "KLARNA", "STRIPE_ACH", "GOOGLE_PAY")

        paymentTypes.forEach { type ->
            coEvery { mockStartNativeUiPaymentUseCase(type) } returns Result.success(mockk())

            NativeUiPaymentMethodViewModel(
                paymentMethodType = type,
                startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
                logReporter = logReporter,
            )

            advanceUntilIdle()

            coVerify(exactly = 1) { mockStartNativeUiPaymentUseCase(type) }
        }
    }
}
