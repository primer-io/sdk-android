package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.ShouldCaptureVaultedCvvUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.domain.usecase.vault.ValidateVaultedCVVUseCase
import io.primer.android.internal.domain.usecase.vault.VaultedCvvFieldsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.scope.PrimerVaultedScope
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel implementing PrimerVaultedScope for managing vaulted payment method operations.
 * Handles fetching, selecting, validating, and submitting vaulted payment methods.
 */
@Suppress("TooManyFunctions", "LongParameterList")
internal class VaultedPaymentMethodSelectionViewModel(
    private val fetchVaultedPaymentMethodsUseCase: FetchVaultedPaymentMethodsUseCase,
    private val submitVaultedPaymentUseCase: SubmitVaultedPaymentUseCase,
    private val validateVaultedCVVUseCase: ValidateVaultedCVVUseCase,
    private val shouldCaptureVaultedCvvUseCase: ShouldCaptureVaultedCvvUseCase,
    private val cvvFieldsUseCase: VaultedCvvFieldsUseCase,
    private val deleteVaultedPaymentMethodUseCase: DeleteVaultedPaymentMethodUseCase,
    private val componentsEventsRepository: ComponentsEventsRepository,
    private val checkoutNavigator: CheckoutNavigator,
) : ViewModel(), PrimerVaultedScope {

    private val _state = MutableStateFlow(PrimerVaultedScope.State())
    override val state: StateFlow<PrimerVaultedScope.State> = _state.asStateFlow()

    init {
        loadVaultedPaymentMethods()
        observeCvvFieldChanges()
    }

    private fun observeCvvFieldChanges() {
        cvvFieldsUseCase.cvvValue
            .onEach { cvv ->
                _state.update { it.copy(cvvValue = cvv) }
            }
            .launchIn(viewModelScope)

        cvvFieldsUseCase.expectedCvvLength
            .onEach { length ->
                _state.update { it.copy(expectedCvvLength = length) }
            }
            .launchIn(viewModelScope)

        cvvFieldsUseCase.isCvvValid
            .onEach { isValid ->
                _state.update { it.copy(isCvvValid = isValid) }
            }
            .launchIn(viewModelScope)
    }

    override suspend fun submit() {
        val paymentMethodId = validateSelectedPaymentMethod() ?: return

        if (shouldPromptForCvv(paymentMethodId)) {
            updateStateToCvvRequired(paymentMethodId)
            return
        }

        processPaymentSubmission(paymentMethodId)
    }

    override suspend fun cvvRecapture() {
        val paymentMethodId = validateSelectedPaymentMethod() ?: return

        updateStateToProcessing(paymentMethodId)

        val additionalData = createCVVAdditionalData(cvvFieldsUseCase.cvvValue.value)
        validateAndSubmitWithCvv(paymentMethodId, additionalData)
    }

    override fun updateCvv(cvv: String) {
        cvvFieldsUseCase.updateCvv(cvv)
    }

    override fun selectPaymentMethod(paymentMethodId: String) {
        // Update first6Digits for CVV length calculation
        val paymentMethod = getPaymentMethodById(paymentMethodId)
        val first6Digits = paymentMethod?.paymentInstrumentData?.first6Digits?.toString() ?: ""
        cvvFieldsUseCase.updateFirst6Digits(first6Digits)

        _state.update { currentState ->
            // If we're in AllMethods stage, return to Selection stage after selection
            val newStage = if (currentState.stage is PrimerVaultedScope.State.Stage.AllMethods) {
                PrimerVaultedScope.State.Stage.Selection
            } else {
                currentState.stage
            }

            currentState.copy(
                selectedPaymentMethodId = paymentMethodId,
                stage = newStage,
                editMode = PrimerVaultedScope.State.EditMode.View,
            )
        }
    }

    override fun clearSelection() {
        cvvFieldsUseCase.clear()
        _state.update { it.copy(selectedPaymentMethodId = null) }
    }

    override fun cancelCvvRecapture() {
        cvvFieldsUseCase.clear()
        _state.update {
            it.copy(
                isCvvRequired = false,
                isProcessing = false,
                isLoading = it.paymentMethods.isEmpty(),
                stage = PrimerVaultedScope.State.Stage.Selection,
            )
        }
    }

    private fun validateSelectedPaymentMethod(): String? {
        val paymentMethodId = _state.value.selectedPaymentMethodId
        if (paymentMethodId == null) {
            _state.update {
                it.copy(error = IllegalStateException("No payment method selected"))
            }
        }
        return paymentMethodId
    }

    private suspend fun shouldPromptForCvv(paymentMethodId: String): Boolean {
        val paymentMethod = getPaymentMethodById(paymentMethodId) ?: return false
        return shouldCaptureVaultedCvvUseCase(paymentMethod).getOrDefault(false)
    }

    private fun updateStateToCvvRequired(paymentMethodId: String) {
        // Update first6Digits for CVV length calculation
        val paymentMethod = getPaymentMethodById(paymentMethodId)
        val first6Digits = paymentMethod?.paymentInstrumentData?.first6Digits?.toString() ?: ""
        cvvFieldsUseCase.updateFirst6Digits(first6Digits)

        _state.update {
            it.copy(
                selectedPaymentMethodId = paymentMethodId,
                isCvvRequired = true,
                isProcessing = false,
                error = null,
                stage = PrimerVaultedScope.State.Stage.Cvv(paymentMethodId),
            )
        }
    }

    private fun updateStateToProcessing(paymentMethodId: String) {
        _state.update { current ->
            // Stay on CVV screen if we're processing a CVV recapture
            val stage = if (current.stage is PrimerVaultedScope.State.Stage.Cvv &&
                current.stage.paymentMethodId == paymentMethodId
            ) {
                current.stage
            } else {
                PrimerVaultedScope.State.Stage.Selection
            }

            current.copy(
                selectedPaymentMethodId = paymentMethodId,
                isProcessing = true,
                error = null,
                stage = stage,
            )
        }
    }

    private suspend fun processPaymentSubmission(paymentMethodId: String) {
        updateStateToProcessing(paymentMethodId)
        logPaymentSubmitted(paymentMethodId)

        submitVaultedPaymentUseCase(paymentMethodId).fold(
            onSuccess = {
                handlePaymentSuccess(paymentMethodId)
            },
            onFailure = { exception ->
                handlePaymentFailure(paymentMethodId, exception)
            },
        )
    }

    private suspend fun validateAndSubmitWithCvv(
        paymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ) {
        validateVaultedCVVUseCase(paymentMethodId, additionalData).fold(
            onSuccess = { validationErrors ->
                if (validationErrors.isEmpty()) {
                    submitPaymentWithCvv(paymentMethodId, additionalData)
                } else {
                    handleCvvValidationFailure(paymentMethodId)
                }
            },
            onFailure = { exception ->
                handlePaymentFailure(paymentMethodId, exception)
            },
        )
    }

    private suspend fun submitPaymentWithCvv(
        paymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ) {
        submitVaultedPaymentUseCase(paymentMethodId, additionalData).fold(
            onSuccess = {
                handlePaymentSuccess(paymentMethodId)
            },
            onFailure = { exception ->
                handlePaymentFailure(paymentMethodId, exception)
            },
        )
    }

    private suspend fun handlePaymentSuccess(paymentMethodId: String) {
        _state.update {
            it.copy(
                selectedPaymentMethodId = paymentMethodId,
                isProcessing = false,
                isCvvRequired = false,
                error = null,
                stage = PrimerVaultedScope.State.Stage.Selection,
            )
        }
        logPaymentSuccess(paymentMethodId)
        checkoutNavigator.navigateToSuccess()
    }

    private suspend fun handlePaymentFailure(paymentMethodId: String, exception: Throwable) {
        _state.update { current ->
            // Stay on CVV screen if we were there, otherwise go to Selection
            val stage = current.stage as? PrimerVaultedScope.State.Stage.Cvv ?: PrimerVaultedScope.State.Stage.Selection

            current.copy(
                selectedPaymentMethodId = paymentMethodId,
                isProcessing = false,
                error = exception,
                stage = stage,
            )
        }
        logPaymentFailure(paymentMethodId)
        checkoutNavigator.navigateToError(exception.message ?: "Payment failed")
    }

    private fun handleCvvValidationFailure(paymentMethodId: String) {
        _state.update {
            it.copy(
                selectedPaymentMethodId = paymentMethodId,
                isCvvRequired = true,
                isProcessing = false,
                stage = PrimerVaultedScope.State.Stage.Cvv(paymentMethodId),
            )
        }
    }

    private fun loadVaultedPaymentMethods() {
        viewModelScope.launch {
            val currentSelection = _state.value.selectedPaymentMethodId
            val currentStage = _state.value.stage
            val currentEditMode = _state.value.editMode

            _state.update { it.copy(isLoading = true) }

            fetchVaultedPaymentMethodsUseCase().fold(
                onSuccess = { vaultedMethods ->
                    val updatedSelection = currentSelection?.takeIf { selection ->
                        vaultedMethods.any { it.id == selection }
                    }

                    _state.update {
                        it.copy(
                            paymentMethods = vaultedMethods,
                            selectedPaymentMethodId = updatedSelection,
                            isLoading = false,
                            error = null,
                            // Preserve the current stage and edit mode instead of resetting
                            stage = currentStage,
                            editMode = currentEditMode,
                        )
                    }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception,
                        )
                    }
                },
            )
        }
    }

    private fun createCVVAdditionalData(cvv: String): PrimerVaultedPaymentMethodAdditionalData {
        return PrimerVaultedCardAdditionalData(cvv = cvv)
    }

    private fun getPaymentMethodById(paymentMethodId: String): PrimerVaultedPaymentMethod? {
        return _state.value.paymentMethods.find { it.id == paymentMethodId }
    }

    private fun logPaymentSubmitted(paymentMethodId: String) {
        val paymentMethodType = getAnalyticsPaymentMethodType(paymentMethodId)
        componentsEventsRepository.send(EventType.PaymentSubmitted(paymentMethodType))
    }

    private fun logPaymentSuccess(paymentMethodId: String) {
        val paymentMethodType = getAnalyticsPaymentMethodType(paymentMethodId)
        // Payment ID is not yet available in the vaulted flow; emit empty value until backend support arrives.
        componentsEventsRepository.send(EventType.PaymentSuccess(paymentMethodType, ""))
    }

    private fun logPaymentFailure(paymentMethodId: String) {
        val paymentMethodType = getAnalyticsPaymentMethodType(paymentMethodId)
        componentsEventsRepository.send(EventType.PaymentFailure(paymentMethodType))
    }

    private fun getAnalyticsPaymentMethodType(paymentMethodId: String): String {
        return getPaymentMethodById(paymentMethodId)?.paymentMethodType ?: paymentMethodId
    }

    // ========== AllMethods Stage Management ==========

    /**
     * Navigates to the AllMethods stage showing full list of vaulted payment methods.
     * Preserves the current selection state.
     */
    override fun showAllMethods() {
        _state.update {
            it.copy(
                stage = PrimerVaultedScope.State.Stage.AllMethods,
                editMode = PrimerVaultedScope.State.EditMode.View,
            )
        }
    }

    /**
     * Returns to the Selection stage from AllMethods.
     * Preserves the current selection state.
     */
    fun returnToSelection() {
        _state.update {
            it.copy(
                stage = PrimerVaultedScope.State.Stage.Selection,
                editMode = PrimerVaultedScope.State.EditMode.View,
                deletingPaymentMethodId = null,
            )
        }
    }

    /**
     * Toggles between View and Edit modes in the AllMethods stage.
     */
    fun toggleEditMode() {
        _state.update {
            val newEditMode = when (it.editMode) {
                PrimerVaultedScope.State.EditMode.View -> PrimerVaultedScope.State.EditMode.Edit
                PrimerVaultedScope.State.EditMode.Edit -> PrimerVaultedScope.State.EditMode.View
            }
            it.copy(
                editMode = newEditMode,
                deletingPaymentMethodId = null,
            )
        }
    }

    /**
     * Shows delete confirmation dialog for a payment method.
     *
     * @param paymentMethodId The ID of the payment method to mark for deletion
     */
    fun showDeleteConfirmation(paymentMethodId: String) {
        _state.update {
            it.copy(deletingPaymentMethodId = paymentMethodId)
        }
    }

    /**
     * Cancels the delete operation and clears the deletion state.
     */
    fun cancelDelete() {
        _state.update {
            it.copy(deletingPaymentMethodId = null)
        }
    }

    /**
     * Confirms and executes deletion of the payment method.
     * Refreshes the payment methods list after deletion.
     * If the deleted method was selected, clears the selection.
     */
    fun confirmDelete() {
        val paymentMethodId = _state.value.deletingPaymentMethodId ?: return
        val wasSelected = _state.value.selectedPaymentMethodId == paymentMethodId

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }

            deleteVaultedPaymentMethodUseCase(paymentMethodId).fold(
                onSuccess = {
                    if (wasSelected) {
                        clearSelection()
                    }

                    // Clear deletion state
                    _state.update {
                        it.copy(
                            deletingPaymentMethodId = null,
                            isDeleting = false,
                        )
                    }

                    // Reload payment methods to reflect the deletion
                    loadVaultedPaymentMethods()
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            deletingPaymentMethodId = null,
                            isDeleting = false,
                            error = exception,
                        )
                    }
                },
            )
        }
    }
}
