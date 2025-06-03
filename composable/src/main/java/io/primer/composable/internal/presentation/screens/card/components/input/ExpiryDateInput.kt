package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.ExpiryDateInput(
    modifier: Modifier = Modifier,
) {
    FormattedInput(
        modifier = modifier,
        type = PrimerInputElementType.EXPIRY_DATE,
        formatter = ExpiryDateFormatter,
    )
}

private object ExpiryDateFormatter : InputFormatter {
    override fun format(value: String): String {
        return when (value.length) {
            0, 1 -> value
            2 -> "$value/"
            else -> "${value.take(2)}/${value.drop(2).take(2)}"
        }
    }

    override fun clean(input: String): String {
        val digits = input.filter { it.isDigit() }.take(4)

        // Validate month (01-12)
        if (digits.length >= 2) {
            val month = digits.substring(0, 2).toIntOrNull() ?: 0
            if (month > 12) {
                return digits.take(1) // Only keep first digit if month is invalid
            }
        }

        return digits
    }
}
