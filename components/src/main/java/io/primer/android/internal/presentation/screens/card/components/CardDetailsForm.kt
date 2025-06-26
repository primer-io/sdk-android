package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.CardDetailsForm(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        cardNumberInput(Modifier.fillMaxWidth())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.large),
        ) {
            expiryDateInput(Modifier.weight(1f))
            cvvInput(Modifier.weight(1f))
        }

        cardholderNameInput(Modifier.fillMaxWidth())
    }
}
