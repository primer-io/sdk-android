package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.CardNumberInput(
    modifier: Modifier = Modifier,
) {
    FormattedInput(
        modifier = modifier,
        type = PrimerInputElementType.CARD_NUMBER,
        formatter = CardNumberFormatter,
    )
}

private object CardNumberFormatter : InputFormatter {
    override fun format(value: String): String {
        return value.chunked(4).joinToString(" ")
    }

    override fun clean(input: String): String {
        return input.filter { it.isDigit() }.take(16)
    }
}
