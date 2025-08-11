package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.scope.PrimerCardFormScope

// TODO check this
@Composable
internal fun PrimerCardFormScope.CardNetwork(modifier: Modifier = Modifier) {
    val state by state.collectAsStateWithLifecycle()
    val networks = state.availableNetworks

    if (networks.isEmpty() || networks.size == 1) {
        CardNetworkIcon()
    } else {
        CardNetworkSelector()
    }
}

@Composable
internal fun PrimerCardFormScope.CardNetworkIcon() {
    val state by state.collectAsStateWithLifecycle()

    Icon(
        painter = painterResource(id = state.selectedNetwork.getCardImageAsset(ImageColor.COLORED)),
        contentDescription = state.selectedNetwork.name,
        tint = Color.Unspecified,
    )
}

@Composable
private fun PrimerCardFormScope.CardNetworkSelector(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()
    val networks = state.availableNetworks
    val selectedNetwork = state.selectedNetwork

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp), // Larger touch area
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardNetworkIcon()
            Icon(
                painter = painterResource(R.drawable.ic_primer_chevron_down),
                contentDescription = stringResource(R.string.primer_components_content_description_select_network),
                tint = LocalPrimerTheme.current.colorTokens().primerColorIconPrimary,
            )
        }

        DropdownMenu(
            containerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            networks.forEachIndexed { index, network ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selectCardNetwork(network.network)
                    },
                    leadingIcon = {
                        if (network.network == selectedNetwork) {
                            Icon(
                                painter = painterResource(R.drawable.ic_primer_check),
                                contentDescription = "Selected",
                                tint = LocalPrimerTheme.current.colorTokens().primerColorIconPrimary,
                            )
                        }
                    },
                    text = {
                        Text(
                            text = network.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                        )
                    },
                )
                if (index < networks.size - 1) { HorizontalDivider() }
            }
        }
    }
}
