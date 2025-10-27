package io.primer.android.internal.presentation.screens.paymentMethodSelection

import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.ShouldCaptureVaultedCvvUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.domain.usecase.vault.ValidateVaultedCVVUseCase
import io.primer.android.internal.domain.usecase.vault.VaultedCvvFieldsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerVaultedScope
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class VaultedPaymentMethodSelectionViewModelTest {

    private lateinit var mockFetchVaultedPaymentMethodsUseCase: FetchVaultedPaymentMethodsUseCase
    private lateinit var mockSubmitVaultedPaymentUseCase: SubmitVaultedPaymentUseCase
    private lateinit var mockValidateVaultedCVVUseCase: ValidateVaultedCVVUseCase
    private lateinit var mockShouldCaptureVaultedCvvUseCase: ShouldCaptureVaultedCvvUseCase
    private lateinit var cvvFieldsUseCase: VaultedCvvFieldsUseCase
    private lateinit var mockComponentsEventsRepository: ComponentsEventsRepository
    private lateinit var mockCheckoutNavigator: CheckoutNavigator

    private lateinit var viewModel: VaultedPaymentMethodSelectionViewModel

    @BeforeEach
    fun setUp() {
        mockFetchVaultedPaymentMethodsUseCase = mockk()
        mockSubmitVaultedPaymentUseCase = mockk()
        mockValidateVaultedCVVUseCase = mockk()
        mockShouldCaptureVaultedCvvUseCase = mockk()
        cvvFieldsUseCase = VaultedCvvFieldsUseCase()
        mockComponentsEventsRepository = mockk(relaxed = true)
        mockCheckoutNavigator = mockk(relaxed = true)
    }

    @Test
    fun `init should load vaulted payment methods successfully`() = runTest {
        val methods = listOf(createVaultedPaymentMethod(id = "pm-1"))
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(methods)
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(any()) } returns Result.success(false)

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(methods, state.paymentMethods)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isCvvRequired)
        assertNull(state.error)
    }

    @Test
    fun `init should emit error when fetching vaulted methods fails`() = runTest {
        val expected = IllegalStateException("boom")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.failure(expected)

        createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(expected, state.error)
    }

    @Test
    fun `submit should emit error when no payment method selected`() = runTest {
        val methods = listOf(createVaultedPaymentMethod())
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(methods)
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(any()) } returns Result.success(false)
        coEvery { mockSubmitVaultedPaymentUseCase.invoke(any(), any()) } returns Result.success(Unit)

        createViewModel()
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.error is IllegalStateException)
    }

    @Test
    fun `submit should transition to CVV required when configuration mandates CVV recapture`() = runTest {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(true)

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(true, state.isCvvRequired)
        assertEquals(method.id, state.selectedPaymentMethodId)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Cvv)
        assertEquals(method.id, (state.stage as PrimerVaultedScope.State.Stage.Cvv).paymentMethodId)
    }

    @Test
    fun `submit should process payment successfully when CVV not required`() = runTest {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(false)
        coEvery { mockSubmitVaultedPaymentUseCase.invoke(any(), any()) } returns Result.success(Unit)

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isCvvRequired)
        assertNull(state.error)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
        verify { mockComponentsEventsRepository.send(match { it is EventType.PaymentSubmitted }, any()) }
        verify { mockComponentsEventsRepository.send(match { it is EventType.PaymentSuccess }, any()) }
        coVerify { mockCheckoutNavigator.navigateToSuccess() }
    }

    @Test
    fun `submit should emit error when payment fails`() = runTest {
        val method = createVaultedPaymentMethod()
        val expected = IllegalStateException("payment failed")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(false)
        coEvery { mockSubmitVaultedPaymentUseCase.invoke(any(), any()) } returns Result.failure(expected)

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isProcessing)
        assertEquals(expected, state.error)
        verify { mockComponentsEventsRepository.send(match { it is EventType.PaymentSubmitted }, any()) }
        verify { mockComponentsEventsRepository.send(match { it is EventType.PaymentFailure }, any()) }
        coVerify { mockCheckoutNavigator.navigateToError(expected.message ?: "") }
    }

    @Test
    fun `cvvRecapture should submit payment when validation succeeds`() = runTest {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockValidateVaultedCVVUseCase.invoke(any(), any()) } returns Result.success(emptyList())
        val additionalDataSlot: CapturingSlot<PrimerVaultedPaymentMethodAdditionalData> = slot()
        coEvery {
            mockSubmitVaultedPaymentUseCase.invoke(any(), capture(additionalDataSlot))
        } returns Result.success(Unit)

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.updateCvv("123")
        viewModel.cvvRecapture()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isCvvRequired)
        assertNull(state.error)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
        assertTrue(additionalDataSlot.captured is PrimerVaultedCardAdditionalData)
        assertEquals("123", (additionalDataSlot.captured as PrimerVaultedCardAdditionalData).cvv)
        verify { mockComponentsEventsRepository.send(match { it is EventType.PaymentSuccess }, any()) }
        coVerify { mockCheckoutNavigator.navigateToSuccess() }
    }

    @Test
    fun `cvvRecapture should remain in CVV required when validation returns errors`() = runTest {
        val method = createVaultedPaymentMethod()
        val validationError = mockk<PrimerValidationError>()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockValidateVaultedCVVUseCase.invoke(any(), any()) } returns Result.success(listOf(validationError))

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.updateCvv("12")
        viewModel.cvvRecapture()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(true, state.isCvvRequired)
        assertEquals(false, state.isProcessing)
        assertEquals(method.id, state.selectedPaymentMethodId)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Cvv)
    }

    @Test
    fun `cvvRecapture should emit error when validation fails`() = runTest {
        val method = createVaultedPaymentMethod()
        val expected = IllegalArgumentException("validation error")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockValidateVaultedCVVUseCase.invoke(any(), any()) } returns Result.failure(expected)

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.updateCvv("123")
        viewModel.cvvRecapture()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(expected, state.error)
    }

    @Test
    fun `cancelCvvRecapture should return to Ready when payment methods exist`() = runTest {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(true)

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceUntilIdle()

        viewModel.cancelCvvRecapture()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isCvvRequired)
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isLoading)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
    }

    @Test
    fun `clearSelection should remove currently selected payment method`() = runTest {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceUntilIdle()

        viewModel.selectPaymentMethod(method.id)
        assertEquals(method.id, viewModel.state.value.selectedPaymentMethodId)

        viewModel.clearSelection()
        advanceUntilIdle()

        assertNull(viewModel.state.value.selectedPaymentMethodId)
    }

    private fun createViewModel() {
        viewModel = VaultedPaymentMethodSelectionViewModel(
            fetchVaultedPaymentMethodsUseCase = mockFetchVaultedPaymentMethodsUseCase,
            submitVaultedPaymentUseCase = mockSubmitVaultedPaymentUseCase,
            validateVaultedCVVUseCase = mockValidateVaultedCVVUseCase,
            shouldCaptureVaultedCvvUseCase = mockShouldCaptureVaultedCvvUseCase,
            cvvFieldsUseCase = cvvFieldsUseCase,
            componentsEventsRepository = mockComponentsEventsRepository,
            checkoutNavigator = mockCheckoutNavigator,
        )
    }

    private fun createVaultedPaymentMethod(
        id: String = "vaulted-id",
        network: String? = null,
    ): PrimerVaultedPaymentMethod {
        return mockk(relaxed = true) {
            every { this@mockk.id } returns id
            every { paymentMethodType } returns PaymentMethodType.PAYMENT_CARD.name
            network?.let {
                every { paymentInstrumentData.network } returns it
            }
        }
    }
}
