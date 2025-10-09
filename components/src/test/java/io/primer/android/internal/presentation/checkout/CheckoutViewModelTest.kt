package io.primer.android.internal.presentation.checkout

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.domain.None
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.scope.PrimerCheckoutScope
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfo
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class CheckoutViewModelTest {

    private lateinit var availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase
    private lateinit var checkoutNavigator: CheckoutNavigator
    private lateinit var componentsEventsRepository: ComponentsEventsRepository
    private lateinit var basicOrderInfoInteractor: BasicOrderInfoInteractor
    private lateinit var configurationRepository: ConfigurationRepository
    private lateinit var viewModel: CheckoutViewModel

    @BeforeEach
    fun setup() {
        availablePaymentMethodsUseCase = mockk()
        checkoutNavigator = mockk(relaxed = true)
        componentsEventsRepository = mockk(relaxed = true)
        basicOrderInfoInteractor = mockk()
        configurationRepository = mockk()

        val orderInfo = BasicOrderInfo(totalAmount = 1000, currencyCode = "USD")
        every { basicOrderInfoInteractor(None) } returns orderInfo
        coEvery { configurationRepository.fetchConfiguration(any()) } returns Result.success(mockk<Configuration>())
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            componentsEventsRepository = componentsEventsRepository,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationRepository = configurationRepository,
        )
    }

    @Test
    fun `should call loadPaymentMethods in init block`() = runTest {
        advanceUntilIdle()
        coVerify(exactly = 1) { configurationRepository.fetchConfiguration(any()) }
        coVerify(exactly = 1) { availablePaymentMethodsUseCase() }
    }

    @Test
    fun `loadPaymentMethods should fetch configuration before loading payment methods`() = runTest {
        advanceUntilIdle()
        coVerify(exactly = 1) { configurationRepository.fetchConfiguration(any()) }
    }

    @Test
    fun `loadPaymentMethods when success should update state to Ready`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value
        assertTrue(state is PrimerCheckoutScope.State.Ready)
        assertEquals(1000, (state as PrimerCheckoutScope.State.Ready).totalAmount)
        assertEquals("USD", state.currencyCode)
    }

    @Test
    fun `loadPaymentMethods when success should call navigateToPaymentMethodsList`() = runTest {
        advanceUntilIdle()
        coVerify(exactly = 1) { checkoutNavigator.navigateToPaymentMethodsList() }
    }

    @Test
    fun `loadPaymentMethods when failure should update state to Error with exception`() = runTest {
        val exception = RuntimeException("Test error")
        coEvery { availablePaymentMethodsUseCase() } returns Result.failure(exception)

        // Reset the viewModel state by creating a new instance for failure scenario
        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            componentsEventsRepository = componentsEventsRepository,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationRepository = configurationRepository,
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

        // Reset the viewModel state by creating a new instance for failure scenario
        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            componentsEventsRepository = componentsEventsRepository,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationRepository = configurationRepository,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateToError(errorMessage) }
    }

    @Test
    fun `loadPaymentMethods when failure with null message should navigate to error with default message`() = runTest {
        val exception = RuntimeException(null as String?)
        coEvery { availablePaymentMethodsUseCase() } returns Result.failure(exception)

        // Reset the viewModel state by creating a new instance for failure scenario
        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            componentsEventsRepository = componentsEventsRepository,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationRepository = configurationRepository,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateToError("Failed to load payment methods") }
    }

    @Test
    fun `onDismiss should update state value to Dismissed`() = runTest {
        viewModel.onDismiss()
        assertEquals(PrimerCheckoutScope.State.Dismissed, viewModel.state.value)
    }

    @Test
    fun `onDismiss should not throw exceptions`() = runTest {
        try {
            viewModel.onDismiss()
        } catch (e: Exception) {
            throw AssertionError("onDismiss should not throw exceptions", e)
        }
    }

    @Test
    fun `onOtherPaymentMethods should call navigateToPaymentMethodsList`() = runTest {
        clearMocks(checkoutNavigator, answers = false, recordedCalls = true) // Clear previous calls

        viewModel.onOtherPaymentMethods()

        coVerify(exactly = 1) { checkoutNavigator.navigateToPaymentMethodsList() }
    }

    @Test
    fun `onRetry should call navigateBack`() = runTest {
        advanceUntilIdle()
        viewModel.onRetry()
        coVerify(exactly = 1) { checkoutNavigator.navigateBack() }
    }

    @Test
    fun `loadPaymentMethods when configuration fetch fails should update state to Error`() = runTest {
        val exception = RuntimeException("Configuration fetch failed")
        coEvery { configurationRepository.fetchConfiguration(any()) } returns Result.failure(exception)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            componentsEventsRepository = componentsEventsRepository,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationRepository = configurationRepository,
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PrimerCheckoutScope.State.Error)
        assertEquals(exception, (state as PrimerCheckoutScope.State.Error).exception)
    }

    @Test
    fun `loadPaymentMethods when configuration fetch fails should navigate to error`() = runTest {
        val errorMessage = "Configuration fetch failed"
        val exception = RuntimeException(errorMessage)
        coEvery { configurationRepository.fetchConfiguration(any()) } returns Result.failure(exception)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            componentsEventsRepository = componentsEventsRepository,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationRepository = configurationRepository,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { checkoutNavigator.navigateToError(errorMessage) }
    }
}
