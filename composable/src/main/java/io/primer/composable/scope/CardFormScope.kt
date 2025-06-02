package io.primer.composable.scope

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import kotlinx.coroutines.flow.StateFlow

interface CardFormScope {

    val state: StateFlow<State>

    fun submit()

    sealed interface State {
        object Loading : State
        data class Ready(
            val cardFields: List<PrimerInputElementType>,
            val billingFields: List<PrimerInputElementType>
        ) : State
    }

}
