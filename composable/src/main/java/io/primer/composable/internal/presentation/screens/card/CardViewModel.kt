package io.primer.composable.internal.presentation.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.composable.internal.domain.interactor.GetRequiredFieldsInteractor
import io.primer.composable.internal.domain.interactor.GetValidationStateInteractor
import io.primer.composable.internal.domain.interactor.SetCardDataInteractor
import io.primer.composable.scope.CardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CardViewModel : ViewModel(), CardFormScope, DISdkComponent {

    private val getAvailableCardFieldsInteractor: GetRequiredFieldsInteractor by lazy { resolve() }

    private val setDataInteractor: SetCardDataInteractor by lazy { resolve() }
    private val getValidationStateInteractor: GetValidationStateInteractor by lazy { resolve() }

    private val logReporter: LogReporter by lazy { resolve() }
    private val actionInteractor: ActionInteractor by lazy { resolve() }

    private val _uiState = MutableStateFlow<CardFormScope.State>(CardFormScope.State())
    override val state: StateFlow<CardFormScope.State> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cardInputFields = getAvailableCardFieldsInteractor.getCardFields()
            val billingInputFields = getAvailableCardFieldsInteractor.getBillingFields()
            _uiState.value = CardFormScope.State(cardInputFields, billingInputFields)
        }
    }

    override fun updateInput(content: Pair<PrimerInputElementType, String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                inputFields = _uiState.value.inputFields + content
            )
        }
        setDataInteractor(content)
    }

    override fun submit() {
        TODO("Not yet implemented")
    }
}
