package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.internal.presentation.screens.card.components.SubmitButton
import io.primer.composable.internal.presentation.screens.card.components.input.Input
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

    companion object {

        @Composable
        fun CardFormScope.PrimerCardFormScreen(
            modifier: Modifier = Modifier,
            content: (@Composable CardFormScope.() -> Unit)? = null,
        ) = content?.invoke(this) ?: CardFormScreen(modifier)

        @Composable
        fun CardFormScope.PrimerSubmitButton(
            modifier: Modifier = Modifier,
            text: String = "Submit",
            content: (@Composable CardFormScope.() -> Unit)? = null,
        ) {
            content?.invoke(this) ?: SubmitButton(
                modifier = modifier,
                text = text,
            )
        }

        @Composable
        fun CardFormScope.PrimerInput(
            modifier: Modifier = Modifier,
            type: PrimerInputElementType,
            content: (@Composable CardFormScope.() -> Unit)? = null,
        ) {
            content?.invoke(this) ?: Input(
                modifier = modifier,
                type = type,
            )
        }

        @Composable
        fun CardFormScope.PrimerCardDetails(
            modifier: Modifier = Modifier,
            content: (@Composable CardFormScope.() -> Unit)? = null,
        ) {
            content?.invoke(this) ?: CardDetailsForm(modifier)
        }

        @Composable
        fun CardFormScope.PrimerBillingAddress(
            modifier: Modifier = Modifier,
            content: (@Composable CardFormScope.() -> Unit)? = null,
        ) {
            content?.invoke(this) ?: BillingAddressForm(modifier)
        }
    }
}
