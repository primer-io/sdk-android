package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

/**
 * Smart input that automatically chooses the right formatter based on input type
 */
@Composable
internal fun CardFormScope.Input(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
) {
    when (type) {
        PrimerInputElementType.CARD_NUMBER -> CardNumberInput(modifier)
        PrimerInputElementType.EXPIRY_DATE -> ExpiryDateInput(modifier)
        PrimerInputElementType.CVV -> CvvInput(modifier)
        PrimerInputElementType.POSTAL_CODE -> PostalCodeInput(modifier)
        else -> FormattedInput(modifier = modifier, type = type)
    }
}
