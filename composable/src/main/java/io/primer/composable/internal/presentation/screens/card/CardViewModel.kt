package io.primer.composable.internal.presentation.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.composable.internal.domain.interactor.GetRequiredFieldsInteractor
import io.primer.composable.scope.CardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CardViewModel : ViewModel(), CardFormScope, DISdkComponent {

    private val getAvailableCardFieldsInteractor: GetRequiredFieldsInteractor by lazy { resolve() }

    private val logReporter: LogReporter by lazy { resolve() }
    private val actionInteractor: ActionInteractor by lazy { resolve() }


    private val _uiState = MutableStateFlow<CardFormScope.State>(CardFormScope.State.Loading)
    override val state: StateFlow<CardFormScope.State> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cardInputFields = getAvailableCardFieldsInteractor.getCardFields()
            val billingInputFields = getAvailableCardFieldsInteractor.getBillingFields()
            _uiState.value = CardFormScope.State.Ready(cardInputFields, billingInputFields)
        }
    }

    override fun submit() {
        TODO("Not yet implemented")
    }

}
