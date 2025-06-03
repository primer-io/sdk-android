package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.PostalCodeInput(
    modifier: Modifier = Modifier,
) {
    FormattedInput(
        modifier = modifier,
        type = PrimerInputElementType.POSTAL_CODE,
        formatter = PostalCodeFormatter,
    )
}

private object PostalCodeFormatter : InputFormatter {
    override fun format(value: String): String = value.uppercase()

    override fun clean(input: String): String {
        // Allow alphanumeric and spaces/dashes for international postal codes
        return input.filter { it.isLetterOrDigit() || it == ' ' || it == '-' }
            .take(10) // Most postal codes are under 10 characters
            .uppercase()
    }
}
