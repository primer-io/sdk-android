package io.primer.android.internal.presentation.screens.vault

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.usecase.vault.CheckCvvRecaptureRequiredUseCase
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class VaultViewModelTest {

    private lateinit var fetchVaultedPaymentMethodsUseCase: FetchVaultedPaymentMethodsUseCase
    private lateinit var submitVaultedPaymentUseCase: SubmitVaultedPaymentUseCase
    private lateinit var checkCvvRecaptureRequiredUseCase: CheckCvvRecaptureRequiredUseCase
    private lateinit var deleteVaultedPaymentMethodUseCase: DeleteVaultedPaymentMethodUseCase
    private lateinit var componentsEventsRepository: ComponentsEventsRepository
    private lateinit var mockCheckoutData: PrimerCheckoutData
    private lateinit var logReporter: LogReporter

    @BeforeEach
    fun setUp() {
        fetchVaultedPaymentMethodsUseCase = mockk()
        submitVaultedPaymentUseCase = mockk()
        checkCvvRecaptureRequiredUseCase = mockk()
        deleteVaultedPaymentMethodUseCase = mockk()
        componentsEventsRepository = mockk(relaxed = true)
        mockCheckoutData = mockk(relaxed = true)
        logReporter = mockk(relaxed = true)
    }

    private fun createViewModel(
        paymentMethods: List<PrimerVaultedPaymentMethod> = emptyList(),
    ): VaultViewModel {
        coEvery { fetchVaultedPaymentMethodsUseCase() } returns Result.success(paymentMethods)
        return VaultViewModel(
            fetchVaultedPaymentMethodsUseCase = fetchVaultedPaymentMethodsUseCase,
            submitVaultedPaymentUseCase = submitVaultedPaymentUseCase,
            checkCvvRecaptureRequiredUseCase = checkCvvRecaptureRequiredUseCase,
            deleteVaultedPaymentMethodUseCase = deleteVaultedPaymentMethodUseCase,
            componentsEventsRepository = componentsEventsRepository,
            logReporter = logReporter,
        )
    }

    private fun createMockPaymentMethod(
        id: String = "pm_123",
        paymentMethodType: String = "PAYMENT_CARD",
        first6Digits: Int? = 411111,
    ): PrimerVaultedPaymentMethod = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.paymentMethodType } returns paymentMethodType
        every { this@mockk.paymentInstrumentData } returns mockk<PaymentInstrumentData> {
            every { this@mockk.first6Digits } returns first6Digits
        }
    }

    @Nested
    inner class InitializationTests {

        @Test
        fun `init should load vaulted payment methods`() = runTest {
            val paymentMethods = listOf(createMockPaymentMethod())

            val viewModel = createViewModel(paymentMethods)
            advanceUntilIdle()

            assertEquals(paymentMethods, viewModel.state.value.paymentMethods)
            coVerify(exactly = 1) { fetchVaultedPaymentMethodsUseCase() }
        }

        @Test
        fun `init should select first payment method`() = runTest {
            val firstMethod = createMockPaymentMethod("pm_1")
            val secondMethod = createMockPaymentMethod("pm_2")
            val paymentMethods = listOf(firstMethod, secondMethod)

            val viewModel = createViewModel(paymentMethods)
            advanceUntilIdle()

            assertEquals(firstMethod, viewModel.state.value.selectedPaymentMethod)
        }

        @Test
        fun `init should set error when fetch fails`() = runTest {
            val exception = RuntimeException("Fetch failed")
            coEvery { fetchVaultedPaymentMethodsUseCase() } returns Result.failure(exception)

            val viewModel = VaultViewModel(
                fetchVaultedPaymentMethodsUseCase = fetchVaultedPaymentMethodsUseCase,
                submitVaultedPaymentUseCase = submitVaultedPaymentUseCase,
                checkCvvRecaptureRequiredUseCase = checkCvvRecaptureRequiredUseCase,
                deleteVaultedPaymentMethodUseCase = deleteVaultedPaymentMethodUseCase,
                componentsEventsRepository = componentsEventsRepository,
                logReporter = logReporter,
            )
            advanceUntilIdle()

            assertEquals(exception, viewModel.state.value.error)
        }

        @Test
        fun `init with empty payment methods should have null selected`() = runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            assertNull(viewModel.state.value.selectedPaymentMethod)
            assertTrue(viewModel.state.value.paymentMethods.isEmpty())
        }
    }

    @Nested
    inner class SelectForPaymentTests {

        @Test
        fun `selectForPayment should update selected payment method`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val paymentMethod = createMockPaymentMethod()
            viewModel.selectForPayment(paymentMethod)
            advanceUntilIdle()

            assertEquals(paymentMethod, viewModel.state.value.selectedPaymentMethod)
        }

        @Test
        fun `selectForPayment should clear cvv`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("123")
            viewModel.selectForPayment(paymentMethod)
            advanceUntilIdle()

            assertNull(viewModel.state.value.cvv)
        }

        @Test
        fun `selectForPayment should clear error`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val paymentMethod = createMockPaymentMethod()
            viewModel.selectForPayment(paymentMethod)
            advanceUntilIdle()

            assertNull(viewModel.state.value.error)
        }

        @Test
        fun `selectForPayment should emit NavigateToSelectedMethod event`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val paymentMethod = createMockPaymentMethod()

            val eventDeferred = async { viewModel.navigation.first() }
            viewModel.selectForPayment(paymentMethod)
            advanceUntilIdle()

            val event = eventDeferred.await()
            assertEquals(VaultViewModel.NavigationEvent.NavigateToSelectedMethod, event)
        }
    }

    @Nested
    inner class SelectForDeletionTests {

        @Test
        fun `selectForDeletion should update selected payment method`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val paymentMethod = createMockPaymentMethod()
            viewModel.selectForDeletion(paymentMethod)
            advanceUntilIdle()

            assertEquals(paymentMethod, viewModel.state.value.selectedPaymentMethod)
        }

        @Test
        fun `selectForDeletion should emit ShowDeleteConfirmation event`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val paymentMethod = createMockPaymentMethod()

            val eventDeferred = async { viewModel.navigation.first() }
            viewModel.selectForDeletion(paymentMethod)
            advanceUntilIdle()

            val event = eventDeferred.await()
            assertEquals(VaultViewModel.NavigationEvent.ShowDeleteConfirmation, event)
        }

        @Test
        fun `selectForDeletion should clear error`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val paymentMethod = createMockPaymentMethod()
            viewModel.selectForDeletion(paymentMethod)
            advanceUntilIdle()

            assertNull(viewModel.state.value.error)
        }
    }

    @Nested
    inner class UpdateCvvTests {

        @Test
        fun `updateCvv should update cvv value in state`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("123")

            assertEquals("123", viewModel.state.value.cvv?.value)
        }

        @Test
        fun `updateCvv should mark cvv as valid for 3 digits`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("123")

            assertTrue(viewModel.state.value.cvv?.isValid == true)
        }

        @Test
        fun `updateCvv should mark cvv as invalid for 2 digits`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("12")

            assertFalse(viewModel.state.value.cvv?.isValid == true)
        }

        @Test
        fun `updateCvv should mark cvv as invalid for non-digits`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("12a")

            assertFalse(viewModel.state.value.cvv?.isValid == true)
        }

        @Test
        fun `updateCvv with empty string should mark as invalid`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("")

            assertFalse(viewModel.state.value.cvv?.isValid == true)
        }
    }

    @Nested
    inner class SubmitTests {

        @Test
        fun `submit without selected payment method should not proceed`() = runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            coVerify(exactly = 0) { checkCvvRecaptureRequiredUseCase(any()) }
        }

        @Test
        fun `submit should show cvv recapture when cvv required and not valid`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns true

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            val eventDeferred = async { viewModel.navigation.first() }
            viewModel.submit()
            advanceUntilIdle()

            val event = eventDeferred.await()
            assertTrue(event is VaultViewModel.NavigationEvent.ShowCvvRecapture)
            assertEquals("pm_123", (event as VaultViewModel.NavigationEvent.ShowCvvRecapture).paymentMethodId)
        }

        @Test
        fun `submit should process payment when cvv not required`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            coVerify(exactly = 1) { submitVaultedPaymentUseCase("pm_123", null) }
        }

        @Test
        fun `submit should process payment when cvv required and valid`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns true
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("123")
            viewModel.submit()
            advanceUntilIdle()

            coVerify(exactly = 1) { submitVaultedPaymentUseCase("pm_123", "123") }
        }

        @Test
        fun `submit should send PaymentSubmitted event`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            verify(exactly = 1) {
                componentsEventsRepository.send(match { it is EventType.PaymentSubmitted }, any())
            }
        }

        @Test
        fun `submit should emit PaymentSuccess on success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            val eventDeferred = async {
                viewModel.navigation.first {
                    it is VaultViewModel.NavigationEvent.PaymentSuccess ||
                        it is VaultViewModel.NavigationEvent.PaymentError
                }
            }
            viewModel.submit()
            advanceUntilIdle()

            val event = eventDeferred.await()
            assertTrue(event is VaultViewModel.NavigationEvent.PaymentSuccess)
        }

        @Test
        fun `submit should send PaymentSuccess event on success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            verify(exactly = 1) {
                componentsEventsRepository.send(match { it is EventType.PaymentSuccess }, any())
            }
        }

        @Test
        fun `submit should clear cvv on success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("123")
            viewModel.submit()
            advanceUntilIdle()

            assertNull(viewModel.state.value.cvv)
        }

        @Test
        fun `submit should emit PaymentError on failure`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.failure(RuntimeException("Failed"))

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            val eventDeferred = async {
                viewModel.navigation.first {
                    it is VaultViewModel.NavigationEvent.PaymentSuccess ||
                        it is VaultViewModel.NavigationEvent.PaymentError
                }
            }
            viewModel.submit()
            advanceUntilIdle()

            val event = eventDeferred.await()
            assertTrue(event is VaultViewModel.NavigationEvent.PaymentError)
        }

        @Test
        fun `submit should set error on failure`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val exception = RuntimeException("Payment failed")
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.failure(exception)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.error)
        }

        @Test
        fun `submit should send PaymentFailure event on failure`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.failure(RuntimeException("Failed"))

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            verify(exactly = 1) {
                componentsEventsRepository.send(match { it is EventType.PaymentFailure }, any())
            }
        }

        @Test
        fun `submit should reset loading state after completion`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { checkCvvRecaptureRequiredUseCase(any()) } returns false
            coEvery { submitVaultedPaymentUseCase(any(), any()) } returns Result.success(mockCheckoutData)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.submit()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isLoading)
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `delete without selected payment method should not call deleteUseCase`() = runTest {
            val viewModel = createViewModel(emptyList())
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            coVerify(exactly = 0) { deleteVaultedPaymentMethodUseCase(any()) }
        }

        @Test
        fun `delete should call deleteVaultedPaymentMethodUseCase with correct id`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            coVerify(exactly = 1) { deleteVaultedPaymentMethodUseCase("pm_123") }
        }

        @Test
        fun `delete should remove payment method from state on success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.paymentMethods.isEmpty())
        }

        @Test
        fun `delete should emit DeleteSuccess event on success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            val eventDeferred = async { viewModel.navigation.first() }
            viewModel.delete()
            advanceUntilIdle()

            val event = eventDeferred.await()
            assertEquals(VaultViewModel.NavigationEvent.DeleteSuccess, event)
        }

        @Test
        fun `delete should select next payment method after deletion`() = runTest {
            val firstMethod = createMockPaymentMethod("pm_1")
            val secondMethod = createMockPaymentMethod("pm_2")
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(firstMethod, secondMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertEquals(secondMethod, viewModel.state.value.selectedPaymentMethod)
            assertEquals(1, viewModel.state.value.paymentMethods.size)
        }

        @Test
        fun `delete should clear cvv on success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.updateCvv("123")
            viewModel.delete()
            advanceUntilIdle()

            assertNull(viewModel.state.value.cvv)
        }

        @Test
        fun `delete should set error on failure`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            val exception = RuntimeException("Delete failed")
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.failure(exception)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertEquals(exception, viewModel.state.value.error)
        }

        @Test
        fun `delete should not remove payment method on failure`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.failure(RuntimeException("Failed"))

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertEquals(1, viewModel.state.value.paymentMethods.size)
        }

        @Test
        fun `delete should reset loading state after success`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isLoading)
        }

        @Test
        fun `delete should reset loading state after failure`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.failure(RuntimeException("Failed"))

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isLoading)
        }

        @Test
        fun `delete last payment method should set selected to null`() = runTest {
            val paymentMethod = createMockPaymentMethod()
            coEvery { deleteVaultedPaymentMethodUseCase(any()) } returns Result.success(Unit)

            val viewModel = createViewModel(listOf(paymentMethod))
            advanceUntilIdle()

            viewModel.delete()
            advanceUntilIdle()

            assertNull(viewModel.state.value.selectedPaymentMethod)
        }
    }
}
