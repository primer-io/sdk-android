package io.primer.android.internal.presentation.checkout

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.Configuration
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
    private lateinit var configurationInteractor: ConfigurationInteractor
    private lateinit var viewModel: CheckoutViewModel

    @BeforeEach
    fun setup() {
        availablePaymentMethodsUseCase = mockk()
        checkoutNavigator = mockk(relaxed = true)
        componentsEventsRepository = mockk(relaxed = true)
        basicOrderInfoInteractor = mockk()
        configurationInteractor = mockk()

        val orderInfo = BasicOrderInfo(totalAmount = 1000, currencyCode = "USD")
        every { basicOrderInfoInteractor(None) } returns orderInfo
        coEvery { configurationInteractor(any()) } returns Result.success(mockk<Configuration>())
        coEvery { availablePaymentMethodsUseCase() } returns Result.success(Unit)

        viewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = checkoutNavigator,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationInteractor = configurationInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )
    }

    @Test
    fun `init should call configurationInteractor, availablePaymentMethodsUseCase and basicOrderInfoInteractor`() = runTest {
        advanceUntilIdle()
        coVerify(exactly = 1) { configurationInteractor(any()) }
        coVerify(exactly = 1) { availablePaymentMethodsUseCase() }
        coVerify(exactly = 1) { basicOrderInfoInteractor(None) }
    }

    @Test
    fun `init when success should update state to Ready`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value
        assertTrue(state is PrimerCheckoutScope.State.Ready)
        assertEquals(1000, (state as PrimerCheckoutScope.State.Ready).totalAmount)
        assertEquals("USD", state.currencyCode)
    }

    @Test
    fun `init when success should call navigateToPaymentMethodsList`() = runTest {
        advanceUntilIdle()
        coVerify(exactly = 1) { checkoutNavigator.navigateToPaymentMethodsList() }
    }

    @Test
    fun `init when success should send SDK_INIT_START and SDK_INIT_END events`() = runTest {
        advanceUntilIdle()
        verify(exactly = 1) { componentsEventsRepository.send(EventType.SdkInitStart, any()) }
        verify(exactly = 1) { componentsEventsRepository.send(EventType.SdkInitEnd, any()) }
    }

    @Test
    fun `init when configurationInteractor fails should update state to Error`() = runTest {
        val exception = RuntimeException("Configuration error")

        val failingConfigInteractor = mockk<ConfigurationInteractor>()
        coEvery { failingConfigInteractor(any()) } returns Result.failure(exception)

        val failingViewModel = CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = mockk(relaxed = true),
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationInteractor = failingConfigInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        advanceUntilIdle()

        val state = failingViewModel.state.value
        assertTrue(state is PrimerCheckoutScope.State.Error)
        assertEquals(exception, (state as PrimerCheckoutScope.State.Error).exception)
    }

    @Test
    fun `init when failure with message should navigate to error with exact message`() = runTest {
        val errorMessage = "Configuration failed"
        val exception = RuntimeException(errorMessage)
        val navigator = mockk<CheckoutNavigator>(relaxed = true)

        val failingConfigInteractor = mockk<ConfigurationInteractor>()
        coEvery { failingConfigInteractor(any()) } returns Result.failure(exception)

        CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = navigator,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationInteractor = failingConfigInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { navigator.navigateToError(errorMessage) }
    }

    @Test
    fun `init when failure with null message should navigate to error with default message`() = runTest {
        val exception = RuntimeException(null as String?)
        val navigator = mockk<CheckoutNavigator>(relaxed = true)

        val failingConfigInteractor = mockk<ConfigurationInteractor>()
        coEvery { failingConfigInteractor(any()) } returns Result.failure(exception)

        CheckoutViewModel(
            availablePaymentMethodsUseCase = availablePaymentMethodsUseCase,
            checkoutNavigator = navigator,
            basicOrderInfoInteractor = basicOrderInfoInteractor,
            configurationInteractor = failingConfigInteractor,
            componentsEventsRepository = componentsEventsRepository,
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { navigator.navigateToError("Failed to load payment methods") }
    }

    @Test
    fun `onDismiss should update state value to Dismissed`() = runTest {
        viewModel.onDismiss()
        assertEquals(PrimerCheckoutScope.State.Dismissed, viewModel.state.value)
    }

    @Test
    fun `onDismiss should send PAYMENT_FLOW_EXITED event`() = runTest {
        viewModel.onDismiss()
        verify(exactly = 1) { componentsEventsRepository.send(EventType.PaymentFlowExited, any()) }
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
        viewModel.onRetry()
        coVerify(exactly = 1) { checkoutNavigator.navigateBack() }
    }

    @Test
    fun `onRetry should send PAYMENT_REATTEMPTED event`() = runTest {
        viewModel.onRetry()
        verify(exactly = 1) { componentsEventsRepository.send(EventType.PaymentReattempted, any()) }
    }
}
