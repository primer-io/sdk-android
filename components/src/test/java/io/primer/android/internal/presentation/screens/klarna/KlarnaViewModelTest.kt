package io.primer.android.internal.presentation.screens.klarna

import android.view.View
import androidx.lifecycle.ViewModelStoreOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.models.KlarnaStep
import io.primer.android.internal.domain.models.KlarnaViewData
import io.primer.android.internal.domain.repositories.KlarnaRepository
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.scope.PrimerKlarnaScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class)
class KlarnaViewModelTest {

    private lateinit var mockViewModelStoreOwner: ViewModelStoreOwner
    private lateinit var mockKlarnaRepository: KlarnaRepository
    private lateinit var mockCheckoutNavigator: CheckoutNavigator
    private lateinit var stepFlow: MutableSharedFlow<KlarnaStep>
    private lateinit var errorFlow: MutableSharedFlow<String>
    private lateinit var viewModel: KlarnaViewModel

    @BeforeEach
    fun setUp() {
        mockViewModelStoreOwner = mockk(relaxed = true)
        mockKlarnaRepository = mockk(relaxed = true)
        mockCheckoutNavigator = mockk(relaxed = true)
        stepFlow = MutableSharedFlow()
        errorFlow = MutableSharedFlow()

        every { mockKlarnaRepository.stepFlow } returns stepFlow
        every { mockKlarnaRepository.errorFlow } returns errorFlow
        coEvery { mockKlarnaRepository.start(any()) } returns Unit
    }

    private fun createViewModel(): KlarnaViewModel {
        return KlarnaViewModel(
            viewModelStoreOwner = mockViewModelStoreOwner,
            klarnaRepository = mockKlarnaRepository,
            checkoutNavigator = mockCheckoutNavigator,
        )
    }

    @Test
    fun `constructor should initialize with Loading step`() = runTest {
        viewModel = createViewModel()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.Loading, state.step)
        assertEquals(emptyList<PrimerKlarnaScope.Category>(), state.categories)
        assertNull(state.selectedCategoryId)
        assertNull(state.paymentView)
    }

    @Test
    fun `init should start klarna repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockKlarnaRepository.start(mockViewModelStoreOwner) }
    }

    @Test
    fun `CategoriesAvailable step updates state with categories`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val categories = listOf(
            KlarnaCategory(
                id = "pay_now",
                displayName = "Pay Now",
                descriptiveAssetUrl = "https://example.com/desc.png",
                standardAssetUrl = "https://example.com/std.png",
            ),
            KlarnaCategory(
                id = "pay_later",
                displayName = "Pay Later",
                descriptiveAssetUrl = "https://example.com/desc2.png",
                standardAssetUrl = "https://example.com/std2.png",
            ),
        )

        stepFlow.emit(KlarnaStep.CategoriesAvailable(categories))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.CategorySelection, state.step)
        assertEquals(2, state.categories.size)
        assertEquals("pay_now", state.categories[0].id)
        assertEquals("Pay Now", state.categories[0].name)
        assertEquals("pay_later", state.categories[1].id)
        assertEquals("Pay Later", state.categories[1].name)
    }

    @Test
    fun `ViewLoaded step updates state with payment view`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val mockView = mockk<View>()
        val viewData = KlarnaViewData(nativeView = mockView)

        stepFlow.emit(KlarnaStep.ViewLoaded(viewData))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.ViewReady, state.step)
        assertEquals(mockView, state.paymentView?.get())
    }

    @Test
    fun `Authorized step with needsFinalization=false navigates to Success`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        stepFlow.emit(KlarnaStep.Authorized(needsFinalization = false))
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateTo(Screen.Success) }
    }

    @Test
    fun `Authorized step with needsFinalization=true updates state to AwaitingFinalization`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        stepFlow.emit(KlarnaStep.Authorized(needsFinalization = true))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.AwaitingFinalization, state.step)
    }

    @Test
    fun `Finalized step navigates to Success`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        stepFlow.emit(KlarnaStep.Finalized)
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateTo(Screen.Success) }
    }

    @Test
    fun `error flow navigates to error screen`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val errorMessage = "Payment failed"
        errorFlow.emit(errorMessage)
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateToError(errorMessage) }
    }

    @Test
    fun `selectPaymentCategory updates state and calls repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val categories = listOf(
            KlarnaCategory(
                id = "pay_now",
                displayName = "Pay Now",
                descriptiveAssetUrl = "https://example.com/desc.png",
                standardAssetUrl = "https://example.com/std.png",
            ),
        )

        stepFlow.emit(KlarnaStep.CategoriesAvailable(categories))
        advanceUntilIdle()

        coEvery { mockKlarnaRepository.selectPaymentCategory(any()) } returns Unit

        viewModel.selectPaymentCategory("pay_now")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("pay_now", state.selectedCategoryId)
        assertEquals(PrimerKlarnaScope.Step.Loading, state.step)

        coVerify(exactly = 1) { mockKlarnaRepository.selectPaymentCategory(categories[0]) }
    }

    @Test
    fun `selectPaymentCategory with unknown categoryId does nothing`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val categories = listOf(
            KlarnaCategory(
                id = "pay_now",
                displayName = "Pay Now",
                descriptiveAssetUrl = "https://example.com/desc.png",
                standardAssetUrl = "https://example.com/std.png",
            ),
        )

        stepFlow.emit(KlarnaStep.CategoriesAvailable(categories))
        advanceUntilIdle()

        viewModel.selectPaymentCategory("unknown_category")
        advanceUntilIdle()

        coVerify(exactly = 0) { mockKlarnaRepository.selectPaymentCategory(any()) }
    }

    @Test
    fun `authorizePayment sets Loading state and calls repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { mockKlarnaRepository.authorizePayment() } returns Unit

        viewModel.authorizePayment()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.Loading, state.step)

        coVerify(exactly = 1) { mockKlarnaRepository.authorizePayment() }
    }

    @Test
    fun `finalizePayment sets Loading state and calls repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { mockKlarnaRepository.finalizePayment() } returns Unit

        viewModel.finalizePayment()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.Loading, state.step)

        coVerify(exactly = 1) { mockKlarnaRepository.finalizePayment() }
    }

    @Test
    fun `onBack from ViewReady returns to CategorySelection`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val mockView = mockk<View>()
        val categories = listOf(
            KlarnaCategory(
                id = "pay_now",
                displayName = "Pay Now",
                descriptiveAssetUrl = "https://example.com/desc.png",
                standardAssetUrl = "https://example.com/std.png",
            ),
        )

        stepFlow.emit(KlarnaStep.CategoriesAvailable(categories))
        advanceUntilIdle()

        viewModel.selectPaymentCategory("pay_now")
        advanceUntilIdle()

        stepFlow.emit(KlarnaStep.ViewLoaded(KlarnaViewData(mockView)))
        advanceUntilIdle()

        assertEquals(PrimerKlarnaScope.Step.ViewReady, viewModel.state.value.step)

        viewModel.onBack()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.CategorySelection, state.step)
        assertNull(state.selectedCategoryId)
        assertNull(state.paymentView)
    }

    @Test
    fun `onBack from other steps calls navigator navigateBack`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onBack()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateBack() }
    }

    @Test
    fun `onCancel calls dismiss on navigator`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCancel()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.dismiss() }
    }

    @Test
    fun `multiple error flows are handled correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        errorFlow.emit("Error 1")
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateToError("Error 1") }

        errorFlow.emit("Error 2")
        advanceUntilIdle()

        coVerify(exactly = 1) { mockCheckoutNavigator.navigateToError("Error 2") }
    }

    @Test
    fun `CategoriesAvailable with empty list updates state correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        stepFlow.emit(KlarnaStep.CategoriesAvailable(emptyList()))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PrimerKlarnaScope.Step.CategorySelection, state.step)
        assertEquals(0, state.categories.size)
    }
}
