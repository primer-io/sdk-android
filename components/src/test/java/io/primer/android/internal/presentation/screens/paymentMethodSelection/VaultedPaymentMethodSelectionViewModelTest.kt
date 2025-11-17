package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.ShouldCaptureVaultedCvvUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.domain.usecase.vault.ValidateVaultedCVVUseCase
import io.primer.android.internal.domain.usecase.vault.VaultedCvvFieldsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerVaultedScope
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultedPaymentMethodSelectionViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)
    private val archTaskExecutor = object : TaskExecutor() {
        override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
        override fun postToMainThread(runnable: Runnable) = runnable.run()
        override fun isMainThread() = true
    }

    private lateinit var mockFetchVaultedPaymentMethodsUseCase: FetchVaultedPaymentMethodsUseCase
    private lateinit var mockSubmitVaultedPaymentUseCase: SubmitVaultedPaymentUseCase
    private lateinit var mockValidateVaultedCVVUseCase: ValidateVaultedCVVUseCase
    private lateinit var mockShouldCaptureVaultedCvvUseCase: ShouldCaptureVaultedCvvUseCase
    private lateinit var cvvFieldsUseCase: VaultedCvvFieldsUseCase
    private lateinit var mockDeleteVaultedPaymentMethodUseCase: DeleteVaultedPaymentMethodUseCase
    private lateinit var mockComponentsEventsRepository: ComponentsEventsRepository
    private lateinit var mockCheckoutNavigator: CheckoutNavigator

    private lateinit var viewModel: VaultedPaymentMethodSelectionViewModel

    @BeforeEach
    fun setUpDispatchers() {
        Dispatchers.setMain(testDispatcher)
        ArchTaskExecutor.getInstance().setDelegate(archTaskExecutor)
    }

    @BeforeEach
    fun setUp() {
        mockFetchVaultedPaymentMethodsUseCase = mockk()
        mockSubmitVaultedPaymentUseCase = mockk()
        mockValidateVaultedCVVUseCase = mockk()
        mockShouldCaptureVaultedCvvUseCase = mockk()
        cvvFieldsUseCase = VaultedCvvFieldsUseCase()
        mockDeleteVaultedPaymentMethodUseCase = mockk()
        mockComponentsEventsRepository = mockk(relaxed = true)
        mockCheckoutNavigator = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDownDispatchers() {
        testScheduler.advanceUntilIdle()
        ArchTaskExecutor.getInstance().setDelegate(null)
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load vaulted payment methods successfully`() = runTest(testDispatcher) {
        val methods = listOf(createVaultedPaymentMethod(id = "pm-1"))
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(methods)
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(any()) } returns Result.success(false)

        createViewModel()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(methods, state.paymentMethods)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isCvvRequired)
        assertNull(state.error)
    }

    @Test
    fun `init should emit error when fetching vaulted methods fails`() = runTest(testDispatcher) {
        val expected = IllegalStateException("boom")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.failure(expected)

        createViewModel()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(expected, state.error)
    }

    @Test
    fun `submit should emit error when no payment method selected`() = runTest(testDispatcher) {
        val methods = listOf(createVaultedPaymentMethod())
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(methods)
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(any()) } returns Result.success(false)
        coEvery { mockSubmitVaultedPaymentUseCase.invoke(any(), any()) } returns Result.success(Unit)

        createViewModel()
        advanceSchedulers()

        viewModel.submit()
        advanceSchedulers()

        val state = viewModel.state.value
        assertTrue(state.error is IllegalStateException)
    }

    @Test
    fun `submit should transition to CVV required when configuration mandates CVV recapture`() = runTest(
        testDispatcher,
    ) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(true)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(true, state.isCvvRequired)
        assertEquals(method.id, state.selectedPaymentMethodId)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Cvv)
        assertEquals(method.id, (state.stage as PrimerVaultedScope.State.Stage.Cvv).paymentMethodId)
    }

    @Test
    fun `submit should process payment successfully when CVV not required`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(false)
        coEvery { mockSubmitVaultedPaymentUseCase.invoke(any(), any()) } returns Result.success(Unit)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isCvvRequired)
        assertNull(state.error)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
        val submittedEventSlot = slot<EventType>()
        val successEventSlot = slot<EventType>()
        verifyOrder {
            mockComponentsEventsRepository.send(capture(submittedEventSlot), any())
            mockComponentsEventsRepository.send(capture(successEventSlot), any())
        }
        val submittedEvent = submittedEventSlot.captured as EventType.PaymentSubmitted
        assertEquals(PaymentMethodType.PAYMENT_CARD.name, submittedEvent.paymentMethod)
        val successEvent = successEventSlot.captured as EventType.PaymentSuccess
        assertEquals(PaymentMethodType.PAYMENT_CARD.name, successEvent.paymentMethod)
        assertTrue(successEvent.paymentId.isEmpty())
        coVerify { mockCheckoutNavigator.navigateToSuccess() }
    }

    @Test
    fun `submit should emit error when payment fails`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        val expected = IllegalStateException("payment failed")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(false)
        coEvery { mockSubmitVaultedPaymentUseCase.invoke(any(), any()) } returns Result.failure(expected)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(false, state.isProcessing)
        assertEquals(expected, state.error)
        val submittedEventSlot = slot<EventType>()
        val failureEventSlot = slot<EventType>()
        verifyOrder {
            mockComponentsEventsRepository.send(capture(submittedEventSlot), any())
            mockComponentsEventsRepository.send(capture(failureEventSlot), any())
        }
        val submittedEvent = submittedEventSlot.captured as EventType.PaymentSubmitted
        assertEquals(PaymentMethodType.PAYMENT_CARD.name, submittedEvent.paymentMethod)
        val failureEvent = failureEventSlot.captured as EventType.PaymentFailure
        assertEquals(PaymentMethodType.PAYMENT_CARD.name, failureEvent.paymentMethod)
        assertNull(failureEvent.paymentId)
        coVerify { mockCheckoutNavigator.navigateToError(expected.message ?: "") }
    }

    @Test
    fun `cvvRecapture should submit payment when validation succeeds`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockValidateVaultedCVVUseCase.invoke(any(), any()) } returns Result.success(emptyList())
        val additionalDataSlot: CapturingSlot<PrimerVaultedPaymentMethodAdditionalData> = slot()
        coEvery {
            mockSubmitVaultedPaymentUseCase.invoke(
                vaultedPaymentMethodId = any(),
                additionalData = capture(additionalDataSlot),
            )
        } returns Result.success(Unit)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.updateCvv("123")
        viewModel.cvvRecapture()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isCvvRequired)
        assertNull(state.error)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
        assertTrue(additionalDataSlot.captured is PrimerVaultedCardAdditionalData)
        assertEquals("123", (additionalDataSlot.captured as PrimerVaultedCardAdditionalData).cvv)
        val successEventSlot = slot<EventType>()
        verify(exactly = 1) {
            mockComponentsEventsRepository.send(capture(successEventSlot), any())
        }
        val successEvent = successEventSlot.captured as EventType.PaymentSuccess
        assertEquals(PaymentMethodType.PAYMENT_CARD.name, successEvent.paymentMethod)
        assertTrue(successEvent.paymentId.isEmpty())
        coVerify { mockCheckoutNavigator.navigateToSuccess() }
    }

    @Test
    fun `cvvRecapture should remain in CVV required when validation returns errors`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        val validationError = mockk<PrimerValidationError>()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockValidateVaultedCVVUseCase.invoke(any(), any()) } returns Result.success(listOf(validationError))

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.updateCvv("12")
        viewModel.cvvRecapture()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(true, state.isCvvRequired)
        assertEquals(false, state.isProcessing)
        assertEquals(method.id, state.selectedPaymentMethodId)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Cvv)
    }

    @Test
    fun `cvvRecapture should emit error when validation fails`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        val expected = IllegalArgumentException("validation error")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockValidateVaultedCVVUseCase.invoke(any(), any()) } returns Result.failure(expected)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.updateCvv("123")
        viewModel.cvvRecapture()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(expected, state.error)
    }

    @Test
    fun `cancelCvvRecapture should return to Ready when payment methods exist`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockShouldCaptureVaultedCvvUseCase.invoke(method) } returns Result.success(true)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.submit()
        advanceSchedulers()

        viewModel.cancelCvvRecapture()
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(false, state.isCvvRequired)
        assertEquals(false, state.isProcessing)
        assertEquals(false, state.isLoading)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
    }

    @Test
    fun `clearSelection should remove currently selected payment method`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        assertEquals(method.id, viewModel.state.value.selectedPaymentMethodId)

        viewModel.clearSelection()
        advanceSchedulers()

        assertNull(viewModel.state.value.selectedPaymentMethodId)
    }

    @Test
    fun `showAllMethods should switch to AllMethods stage and reset edit mode`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        viewModel.showAllMethods()

        val state = viewModel.state.value
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.AllMethods)
        assertEquals(PrimerVaultedScope.State.EditMode.View, state.editMode)
    }

    @Test
    fun `returnToSelection should restore selection stage and clear deletion state`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        viewModel.showAllMethods()
        viewModel.showDeleteConfirmation(method.id)
        viewModel.returnToSelection()

        val state = viewModel.state.value
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
        assertEquals(PrimerVaultedScope.State.EditMode.View, state.editMode)
        assertNull(state.deletingPaymentMethodId)
    }

    @Test
    fun `toggleEditMode should alternate between view and edit states`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        val initialMode = viewModel.state.value.editMode
        viewModel.toggleEditMode()
        assertEquals(
            PrimerVaultedScope.State.EditMode.Edit,
            viewModel.state.value.editMode,
        )

        viewModel.toggleEditMode()
        assertEquals(initialMode, viewModel.state.value.editMode)
    }

    @Test
    fun `showDeleteConfirmation and cancelDelete should update deletion state`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        viewModel.showDeleteConfirmation(method.id)
        assertEquals(method.id, viewModel.state.value.deletingPaymentMethodId)

        viewModel.cancelDelete()
        assertNull(viewModel.state.value.deletingPaymentMethodId)
    }

    @Test
    fun `toggleEditMode should clear pending deletion state`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        viewModel.showDeleteConfirmation(method.id)
        assertEquals(method.id, viewModel.state.value.deletingPaymentMethodId)

        viewModel.toggleEditMode()
        advanceSchedulers()

        assertNull(viewModel.state.value.deletingPaymentMethodId)
    }

    @Test
    fun `selectPaymentMethod from AllMethods stage should return to selection view`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))

        createViewModel()
        advanceSchedulers()

        viewModel.showAllMethods()
        advanceSchedulers()
        assertTrue(viewModel.state.value.stage is PrimerVaultedScope.State.Stage.AllMethods)

        viewModel.selectPaymentMethod(method.id)
        advanceSchedulers()

        val state = viewModel.state.value
        assertEquals(method.id, state.selectedPaymentMethodId)
        assertTrue(state.stage is PrimerVaultedScope.State.Stage.Selection)
        assertEquals(PrimerVaultedScope.State.EditMode.View, state.editMode)
    }

    @Test
    fun `confirmDelete should clear selection and reload methods on success`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returnsMany listOf(
            Result.success(listOf(method)),
            Result.success(emptyList()),
        )
        coEvery { mockDeleteVaultedPaymentMethodUseCase.invoke(any()) } returns Result.success(Unit)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.showDeleteConfirmation(method.id)
        advanceSchedulers()
        assertEquals(method.id, viewModel.state.value.deletingPaymentMethodId)
        viewModel.confirmDelete()
        advanceSchedulers()

        coVerify { mockDeleteVaultedPaymentMethodUseCase.invoke(any()) }

        val state = viewModel.state.value
        assertNull(state.selectedPaymentMethodId)
        assertTrue(state.paymentMethods.isEmpty())
        assertFalse(state.isDeleting)
        assertNull(state.deletingPaymentMethodId)
    }

    @Test
    fun `confirmDelete should expose error when deletion fails`() = runTest(testDispatcher) {
        val method = createVaultedPaymentMethod()
        val expected = IllegalStateException("delete failed")
        coEvery { mockFetchVaultedPaymentMethodsUseCase() } returns Result.success(listOf(method))
        coEvery { mockDeleteVaultedPaymentMethodUseCase.invoke(any()) } returns Result.failure(expected)

        createViewModel()
        advanceSchedulers()

        viewModel.selectPaymentMethod(method.id)
        viewModel.showDeleteConfirmation(method.id)
        advanceSchedulers()
        assertEquals(method.id, viewModel.state.value.deletingPaymentMethodId)
        viewModel.confirmDelete()
        advanceSchedulers()

        coVerify { mockDeleteVaultedPaymentMethodUseCase.invoke(any()) }

        val state = viewModel.state.value
        assertEquals(expected, state.error)
        assertEquals(method.id, state.selectedPaymentMethodId)
        assertFalse(state.isDeleting)
        assertNull(state.deletingPaymentMethodId)
    }

    private suspend fun TestScope.advanceSchedulers() {
        this.advanceUntilIdle()
        testScheduler.advanceUntilIdle()
    }

    private fun createViewModel() {
        viewModel = VaultedPaymentMethodSelectionViewModel(
            fetchVaultedPaymentMethodsUseCase = mockFetchVaultedPaymentMethodsUseCase,
            submitVaultedPaymentUseCase = mockSubmitVaultedPaymentUseCase,
            validateVaultedCVVUseCase = mockValidateVaultedCVVUseCase,
            shouldCaptureVaultedCvvUseCase = mockShouldCaptureVaultedCvvUseCase,
            cvvFieldsUseCase = cvvFieldsUseCase,
            deleteVaultedPaymentMethodUseCase = mockDeleteVaultedPaymentMethodUseCase,
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
