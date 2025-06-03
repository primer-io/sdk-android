package io.primer.composable.scope

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import kotlinx.coroutines.flow.StateFlow

interface CardFormScope {

    val state: StateFlow<State>

    fun updateInput(content: Pair<PrimerInputElementType, String>)

    fun submit()

    data class State(
        val cardFields: List<PrimerInputElementType> = emptyList(),
        val billingFields: List<PrimerInputElementType> = emptyList(),
        val fieldErrors: List<PrimerInputValidationError> = emptyList(),
        val inputFields: Map<PrimerInputElementType, String> = emptyMap(),
    )
}
