package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.CardDetailsForm(
    modifier: Modifier = Modifier,
) {
    val spacingSmall = LocalPrimerSpacingTokens.current.small
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        with(components) {
            cardNumberInput(Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacingSmall))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                expiryDateInput(Modifier.weight(1f))
                Spacer(modifier = Modifier.width(spacingSmall))
                cvvInput(Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(spacingSmall))
            cardholderNameInput(Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(spacingSmall))
        }
    }
}
