package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.primer.android.LocalPrimerTheme
import io.primer.android.api.components.card.PrimerCardFormController
import io.primer.android.components.R
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultCardNetworkIcon(
    networkSelection: PrimerCardFormController.NetworkSelection,
    onNetworkSelected: (PrimerCardNetwork) -> Unit,
) {
    val networks = networkSelection.availableNetworks
    when {
        networks.size <= 1 -> CardNetworkIcon(network = networkSelection.selectedNetwork?.network)
        networkSelection.isNetworkSelectable.not() -> CardNetworkDisplayOnly(networks)
        else -> CardNetworkSelector(networkSelection = networkSelection, onNetworkSelected = onNetworkSelected)
    }
}

@Composable
private fun CardNetworkDisplayOnly(networks: List<PrimerCardNetwork>) {
    val theme = LocalPrimerTheme.current
    Row(
        modifier = Modifier.padding(horizontal = theme.spacingTokens.medium),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        networks.forEach { CardNetworkIcon(network = it.network) }
    }
}

@Suppress("LongMethod")
@Composable
private fun CardNetworkSelector(
    networkSelection: PrimerCardFormController.NetworkSelection,
    onNetworkSelected: (PrimerCardNetwork) -> Unit,
) {
    val networks = networkSelection.availableNetworks
    val selectedNetwork = networkSelection.selectedNetwork
    var expanded by remember { mutableStateOf(false) }
    val theme = LocalPrimerTheme.current

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = theme.spacingTokens.medium, vertical = theme.spacingTokens.small),
            horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardNetworkIcon(network = networkSelection.selectedNetwork?.network)
            Icon(
                painter = painterResource(R.drawable.ic_primer_chevron_down),
                contentDescription = stringResource(R.string.accessibility_card_form_network_selector),
                tint = theme.colorTokens().primerColorIconPrimary,
            )
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(
                            start = theme.spacingTokens.xlarge,
                            end = theme.spacingTokens.xlarge,
                            top = theme.sizeTokens.xxlarge,
                        ),
                    elevation = CardDefaults.elevatedCardElevation(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.colorTokens().primerColorBackground,
                    ),
                ) {
                    Column {
                        networks.forEachIndexed { index, network ->
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    onNetworkSelected(network)
                                },
                                leadingIcon = {
                                    CardNetworkIcon(network = network.network)
                                },
                                text = {
                                    Text(
                                        text = network.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = theme.colorTokens().primerColorTextPrimary,
                                    )
                                },
                                trailingIcon = {
                                    if (network == selectedNetwork) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_primer_check),
                                            contentDescription = stringResource(R.string.accessibility_common_selected),
                                            tint = theme.colorTokens().primerColorIconPrimary,
                                        )
                                    }
                                },
                            )
                            if (index < networks.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultCardNetworkIconPreview() = PreviewContainer {
    DefaultCardNetworkIcon(
        networkSelection = PrimerCardFormController.NetworkSelection(),
        onNetworkSelected = { },
    )
}
