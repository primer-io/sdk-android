package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.scope.PrimerCardFormScope
import io.primer.android.internal.presentation.theme.LocalPrimerSpacingTokens

@Composable
internal fun PrimerCardFormScope.CardDetailsForm(
    modifier: Modifier = Modifier,
) {
    val uiState by state.collectAsState()
    
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Card number input with network selector overlay
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            cardNumberInput(Modifier.fillMaxWidth())
            
            // Show network selector when networks are available
            if (uiState.availableNetworks.isNotEmpty()) {
                cardNetworkSelector(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
                    uiState.availableNetworks,
                    uiState.selectedNetwork,
                    ::selectCardNetwork
                )
            }
        }

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
