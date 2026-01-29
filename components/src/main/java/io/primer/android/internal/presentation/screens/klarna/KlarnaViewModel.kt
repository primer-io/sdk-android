package io.primer.android.internal.presentation.screens.klarna

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.error.PrimerErrorException
import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.models.KlarnaStep
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.KlarnaRepository
import io.primer.android.internal.domain.usecase.KlarnaCleanupUseCase
import io.primer.android.scope.PrimerKlarnaScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class KlarnaViewModel(
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val klarnaRepository: KlarnaRepository,
    private val headlessRepository: HeadlessRepository,
    private val cleanupUseCase: KlarnaCleanupUseCase,
    private val logReporter: LogReporter,
) : ViewModel(), PrimerKlarnaScope, Cleanable {

    sealed interface NavigationEvent {
        data class Success(val checkoutData: PrimerCheckoutData) : NavigationEvent
        data class Error(val error: PrimerError) : NavigationEvent
    }

    private val _state = MutableStateFlow(
        PrimerKlarnaScope.State(
            step = PrimerKlarnaScope.Step.Loading,
            categories = emptyList(),
            selectedCategoryId = null,
            paymentView = null,
        ),
    )
    override val state: StateFlow<PrimerKlarnaScope.State> = _state.asStateFlow()

    private val _navigation = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()

    private var domainCategories: List<KlarnaCategory> = emptyList()

    init {
        logReporter.info("Initializing Klarna payment", component = TAG)
        viewModelScope.launch {
            klarnaRepository.start(viewModelStoreOwner)
            launch { collectSteps() }
            launch { collectErrors() }
        }
    }

    private suspend fun collectSteps() {
        klarnaRepository.stepFlow.collect { step ->
            logReporter.debug("Klarna step: ${step::class.simpleName}", component = TAG)
            when (step) {
                is KlarnaStep.CategoriesAvailable -> {
                    logReporter.debug(
                        "Klarna categories available: ${step.categories.size}",
                        component = TAG,
                    )
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
                        logReporter.info("Klarna payment authorized successfully", component = TAG)
                        awaitAndEmitResult()
                    } else {
                        logReporter.debug("Klarna payment awaiting finalization", component = TAG)
                        _state.update {
                            it.copy(step = PrimerKlarnaScope.Step.AwaitingFinalization)
                        }
                    }
                }

                is KlarnaStep.Finalized -> {
                    awaitAndEmitResult()
                    logReporter.info("Klarna payment finalized successfully", component = TAG)
                }
            }
        }
    }

    private suspend fun collectErrors() {
        klarnaRepository.errorFlow.collect { errorMessage ->
            logReporter.error("Klarna error: $errorMessage", component = TAG)
            _navigation.trySend(NavigationEvent.Error(PrimerUnknownError(errorMessage)))
        }
    }

    private suspend fun awaitAndEmitResult() {
        headlessRepository.awaitPaymentResult().fold(
            onSuccess = { checkoutData ->
                _navigation.trySend(NavigationEvent.Success(checkoutData))
            },
            onFailure = { error ->
                val primerError = (error as? PrimerErrorException)?.primerError
                    ?: PrimerUnknownError(error.message ?: "Payment failed")
                _navigation.trySend(NavigationEvent.Error(primerError))
            },
        )
    }

    override fun selectPaymentCategory(context: Context, categoryId: String) {
        logReporter.debug("Klarna category selected: $categoryId", component = TAG)
        viewModelScope.launch {
            val category = domainCategories.find { it.id == categoryId }
                ?: return@launch

            _state.update {
                it.copy(
                    selectedCategoryId = categoryId,
                    step = PrimerKlarnaScope.Step.Loading,
                )
            }

            klarnaRepository.selectPaymentCategory(context, category)
        }
    }

    override fun authorizePayment() {
        logReporter.debug("Klarna authorization requested", component = TAG)
        viewModelScope.launch {
            _state.update {
                it.copy(step = PrimerKlarnaScope.Step.AuthorizationStarted)
            }
            klarnaRepository.authorizePayment()
        }
    }

    override fun finalizePayment() {
        logReporter.debug("Klarna finalization requested", component = TAG)
        viewModelScope.launch {
            _state.update {
                it.copy(step = PrimerKlarnaScope.Step.Loading)
            }
            klarnaRepository.finalizePayment()
        }
    }

    override fun cleanup() {
        cleanupUseCase.cleanup()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    companion object {
        private const val TAG = "KlarnaViewModel"
    }
}
