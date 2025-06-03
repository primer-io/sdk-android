package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.CvvInput(
    modifier: Modifier = Modifier,
) {
    FormattedInput(
        modifier = modifier,
        type = PrimerInputElementType.CVV,
        formatter = CvvFormatter,
    )
}

private object CvvFormatter : InputFormatter {
    override fun format(value: String): String = value

    override fun clean(input: String): String {
        // CVV is 3 digits for most cards, 4 for Amex
        return input.filter { it.isDigit() }.take(4)
    }
}
