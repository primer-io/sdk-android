package io.primer.android.internal.presentation.checkout

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class CheckoutViewModelTest {

    private lateinit var availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase
    private lateinit var checkoutNavigator: CheckoutNavigator
    private lateinit var viewModel: CheckoutViewModel

    @BeforeEach
    fun setup() {
        availablePaymentMethodsUseCase = mockk()
        checkoutNavigator = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `constructor should store dependencies correctly`() {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        assertNotNull(viewModel)
    }

    @Test
    fun `should initialize state with Initializing`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        val initialState = viewModel.state.first()
        assertTrue(initialState is PrimerCheckoutScope.State.Initializing)
    }

    @Test
    fun `should call loadPaymentMethods in init block`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { availablePaymentMethodsUseCase() }
    }

    @Test
    fun `loadPaymentMethods when success should update state to Ready`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        assertEquals(PrimerCheckoutScope.State.Ready, viewModel.state.value)
    }

    @Test
    fun `loadPaymentMethods when success should call navigateToPaymentMethodsList`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateToPaymentMethodsList() }
    }

    @Test
    fun `loadPaymentMethods when failure should update state to Error with exception`() = runTest {
        val exception = RuntimeException("Test error")
        coEvery { availablePaymentMethodsUseCase() } returns Result.failure(exception)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PrimerCheckoutScope.State.Error)
        assertEquals(exception, (state as PrimerCheckoutScope.State.Error).exception)
    }

    @Test
    fun `loadPaymentMethods when failure with message should navigate to error with exact message`() = runTest {
        val errorMessage = "Payment methods failed to load"
        val exception = RuntimeException(errorMessage)
        coEvery { availablePaymentMethodsUseCase() } returns Result.failure(exception)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateToError(errorMessage) }
    }

    @Test
    fun `loadPaymentMethods when failure with null message should navigate to error with default message`() = runTest {
        val exception = RuntimeException(null as String?)
        coEvery { availablePaymentMethodsUseCase() } returns Result.failure(exception)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateToError("Failed to load payment methods") }
    }

    @Test
    fun `onDismiss should update state value to Dismissed`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        viewModel.onDismiss()

        assertEquals(PrimerCheckoutScope.State.Dismissed, viewModel.state.value)
    }

    @Test
    fun `onDismiss should not throw exceptions`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        try {
            viewModel.onDismiss()
        } catch (e: Exception) {
            throw AssertionError("onDismiss should not throw exceptions", e)
        }
    }

    @Test
    fun `onOtherPaymentMethods should call navigateToPaymentMethodsList`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        viewModel.onOtherPaymentMethods()

        coVerify(exactly = 1) { checkoutNavigator.navigateToPaymentMethodsList() }
    }

    @Test
    fun `onRetry should call navigateBack`() = runTest {
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
        )

        advanceUntilIdle()

        viewModel.onRetry()

        coVerify(exactly = 1) { checkoutNavigator.navigateBack() }
    }
}
