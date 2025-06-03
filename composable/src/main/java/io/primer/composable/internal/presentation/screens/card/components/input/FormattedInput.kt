package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.FormattedInput(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
    formatter: InputFormatter = NoOpFormatter,
) {
    val state by state.collectAsState()
    val rawValue = state.inputFields[type] ?: ""
    val displayValue = formatter.format(rawValue)

    BaseInput(
        modifier = modifier,
        type = type,
        value = displayValue,
        onValueChange = { newValue ->
            val cleanedValue = formatter.clean(newValue)
            updateInput(type to cleanedValue)
        },
    )
}
