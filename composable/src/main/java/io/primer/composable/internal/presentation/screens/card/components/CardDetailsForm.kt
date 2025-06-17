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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.primer.composable.internal.presentation.screens.card.components.input.CardNumberInput
import io.primer.composable.internal.presentation.screens.card.components.input.CardholderNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.CvvInput
import io.primer.composable.internal.presentation.screens.card.components.input.ExpiryDateInput
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.CardDetailsForm(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val cardInputFields = state.cardFields
    if (cardInputFields.isEmpty()) return

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

        CardNumberInput(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalPrimerSpacingTokens.current.medium),
        ) {
            ExpiryDateInput(
                modifier = Modifier.weight(1f),
            )
            CvvInput(
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.small))

        CardholderNameInput(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(LocalPrimerSpacingTokens.current.large))
    }
}
