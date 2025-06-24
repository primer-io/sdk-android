package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.configuration.data.model.CardNetwork

@Composable
internal fun CardNetworkSelector(
    modifier: Modifier = Modifier,
    networks: List<PrimerCardNetwork>,
    selectedNetwork: CardNetwork.Type?,
    onNetworkSelected: (CardNetwork.Type) -> Unit
) {
    when (networks.size) {
        0 -> {
            CardNetworkIcon(
                modifier = modifier,
                networkType = null
            )
        }
        1 -> {
            CardNetworkIcon(
                modifier = modifier,
                networkType = networks.first().network
            )
        }
        else -> {
            CardNetworkDropdown(
                modifier = modifier,
                networks = networks,
                selectedNetwork = selectedNetwork ?: networks.first().network,
                onNetworkSelected = onNetworkSelected
            )
        }
    }
}

@Composable
private fun CardNetworkDropdown(
    modifier: Modifier = Modifier,
    networks: List<PrimerCardNetwork>,
    selectedNetwork: CardNetwork.Type,
    onNetworkSelected: (CardNetwork.Type) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardNetworkIcon(
                networkType = selectedNetwork,
                size = 24
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select network",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            networks.forEach { network ->
                DropdownMenuItem(
                    onClick = {
                        onNetworkSelected(network.network)
                        expanded = false
                    },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CardNetworkIcon(
                                networkType = network.network,
                                size = 20
                            )
                            Text(
                                text = network.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                )
            }
        }
    }
}