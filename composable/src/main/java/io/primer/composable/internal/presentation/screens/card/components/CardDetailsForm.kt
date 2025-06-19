package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.CardDetailsForm(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "CARD DETAILS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = LocalPrimerSpacingTokens.current.large),
        )

        cardNumberInput(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.medium),
        ) {
            expiryDateInput(Modifier.weight(1f))
            cvvInput(Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.small))

        cardholderNameInput(Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.large))
    }
}
