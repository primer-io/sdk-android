package io.primer.android.internal.presentation.screens.nativeUi

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var mockCheckoutNavigator: CheckoutNavigator
    private lateinit var mockStartNativeUiPaymentUseCase: StartNativeUiPaymentUseCase
    private lateinit var viewModel: NativeUiPaymentMethodViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val paymentMethodType = "GOOGLE_PAY"

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockCheckoutNavigator = mockk(relaxed = true)
        mockStartNativeUiPaymentUseCase = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init starts payment flow and navigates to success on successful payment`() = runTest {
        val mockCheckoutData = mockk<PrimerCheckoutData>()

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockCheckoutData)
        coEvery { mockCheckoutNavigator.navigateTo(any()) } just Runs

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isProcessing)
        assertNull(state.error)

        coVerify(exactly = 1) { mockStartNativeUiPaymentUseCase(paymentMethodType) }
        coVerify(exactly = 1) { mockCheckoutNavigator.navigateTo(Screen.Success) }
    }

    @Test
    fun `init starts payment flow and navigates to error on failed payment`() = runTest {
        val errorMessage = "Payment failed"
        val error = RuntimeException(errorMessage)

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.failure(error)
        coEvery { mockCheckoutNavigator.navigateTo(any()) } just Runs

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isProcessing)
        assertEquals(errorMessage, state.error)

        coVerify(exactly = 1) { mockStartNativeUiPaymentUseCase(paymentMethodType) }
        coVerify(exactly = 1) { mockCheckoutNavigator.navigateTo(Screen.Error) }
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
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        val initialState = viewModel.state.value
        assertTrue(initialState.isProcessing)
        assertNull(initialState.error)
    }

    @Test
    fun `onCancel dismisses checkout navigator`() = runTest {
        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockk())
        coEvery { mockCheckoutNavigator.dismiss() } just Runs

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        viewModel.onCancel()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.dismiss() }
    }

    @Test
    fun `onCleared cleans up payment use case`() = runTest {
        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockk())
        justRun { mockStartNativeUiPaymentUseCase.cleanup() }

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        val onClearedMethod = viewModel::class.java.getDeclaredMethod("onCleared")
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        verify(exactly = 1) { mockStartNativeUiPaymentUseCase.cleanup() }
    }

    @Test
    fun `error with null message is handled correctly`() = runTest {
        val error = RuntimeException()

        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.failure(error)
        coEvery { mockCheckoutNavigator.navigateTo(any()) } just Runs

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isProcessing)
        assertNull(state.error)

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateTo(Screen.Error) }
    }

    @Test
    fun `state flow is properly exposed as StateFlow`() = runTest {
        coEvery { mockStartNativeUiPaymentUseCase(paymentMethodType) } returns Result.success(mockk())

        viewModel = NativeUiPaymentMethodViewModel(
            paymentMethodType = paymentMethodType,
            checkoutNavigator = mockCheckoutNavigator,
            startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
        )

        assertNotNull(viewModel.state)
        // State is always StateFlow by declaration, this test validates it's properly initialized
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
                checkoutNavigator = mockCheckoutNavigator,
                startNativeUiPaymentUseCase = mockStartNativeUiPaymentUseCase,
            )

            advanceUntilIdle()

            coVerify(exactly = 1) { mockStartNativeUiPaymentUseCase(type) }
        }
    }
}
