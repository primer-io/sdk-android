package io.primer.android.internal.presentation.screens.klarna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.models.KlarnaStep
import io.primer.android.internal.domain.repositories.KlarnaRepository
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.scope.PrimerKlarnaScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class KlarnaViewModel(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val klarnaRepository: KlarnaRepository,
    private val checkoutNavigator: CheckoutNavigator,
) : ViewModel(), PrimerKlarnaScope {

    private val _state = MutableStateFlow(
        PrimerKlarnaScope.State(
            step = PrimerKlarnaScope.Step.Loading,
            categories = emptyList(),
            selectedCategoryId = null,
            paymentView = null,
        ),
    )
    override val state: StateFlow<PrimerKlarnaScope.State> = _state.asStateFlow()

    private var domainCategories: List<KlarnaCategory> = emptyList()

    init {
        viewModelScope.launch {
            klarnaRepository.start(viewModelStoreOwner)
            launch { collectSteps() }
            launch { collectErrors() }
        }
    }

    private suspend fun collectSteps() {
        klarnaRepository.stepFlow.collect { step ->
            when (step) {
                is KlarnaStep.CategoriesAvailable -> {
                    domainCategories = step.categories
                    _state.update {
                        it.copy(
                            step = PrimerKlarnaScope.Step.CategorySelection,
                            categories = step.categories.map { category ->
                                PrimerKlarnaScope.Category(
                                    id = category.id,
                                    name = category.displayName,
                                    url = category.standardAssetUrl,
                                )
                            },
                        )
                    }
                }

                is KlarnaStep.ViewLoaded -> {
                    _state.update {
                        it.copy(
                            step = PrimerKlarnaScope.Step.ViewReady,
                            paymentView = WeakReference(step.viewData.nativeView),
                        )
                    }
                }

                is KlarnaStep.Authorized -> {
                    if (!step.needsFinalization) {
                        checkoutNavigator.navigateTo(Screen.Success)
                    } else {
                        _state.update {
                            it.copy(step = PrimerKlarnaScope.Step.AwaitingFinalization)
                        }
                    }
                }

                is KlarnaStep.Finalized -> {
                    checkoutNavigator.navigateTo(Screen.Success)
                }
            }
        }
    }

    private suspend fun collectErrors() {
        klarnaRepository.errorFlow.collect {
            checkoutNavigator.navigateToError(it)
        }
    }

    override fun selectPaymentCategory(categoryId: String) {
        viewModelScope.launch {
            val category = domainCategories.find { it.id == categoryId }
                ?: return@launch

            _state.update {
                it.copy(
                    selectedCategoryId = categoryId,
                    step = PrimerKlarnaScope.Step.Loading,
                )
            }

            klarnaRepository.selectPaymentCategory(category)
        }
    }

    override fun authorizePayment() {
        viewModelScope.launch {
            _state.update {
                it.copy(step = PrimerKlarnaScope.Step.Loading)
            }
            klarnaRepository.authorizePayment()
        }
    }

    override fun finalizePayment() {
        viewModelScope.launch {
            _state.update {
                it.copy(step = PrimerKlarnaScope.Step.Loading)
            }
            klarnaRepository.finalizePayment()
        }
    }

    override fun onBack() {
        when (_state.value.step) {
            PrimerKlarnaScope.Step.ViewReady -> {
                _state.update {
                    it.copy(
                        step = PrimerKlarnaScope.Step.CategorySelection,
                        selectedCategoryId = null,
                        paymentView = null,
                    )
                }
            }
            else -> {
                viewModelScope.launch {
                    checkoutNavigator.navigateBack()
                }
            }
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
