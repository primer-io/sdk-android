package io.primer.android.internal.presentation.screens.vault.components.vaultItem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Suppress("LongMethod")
@Composable
internal fun VaultedItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    expiry: (@Composable () -> Unit)? = null,
    cvvLayout: (@Composable () -> Unit)? = null,
) {
    val theme = LocalPrimerTheme.current
    val shape = RoundedCornerShape(theme.radiusTokens.medium)
    val borderColor = if (isSelected) {
        theme.colorTokens().primerColorBorderOutlinedSelected
    } else {
        theme.colorTokens().primerColorBorderOutlinedDefault
    }
    val selectedStateDescription = if (isSelected) {
        stringResource(R.string.accessibility_common_selected)
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(theme.colorTokens().primerColorBackground)
            .border(theme.borderWidthTokens.thin, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .then(
                if (selectedStateDescription != null) {
                    Modifier.semantics { stateDescription = selectedStateDescription }
                } else {
                    Modifier
                },
            )
            .padding(theme.spacingTokens.medium),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
    ) {
        // Main content row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
            ) {
                // Row 1: Title | Trailing (card number)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f, fill = false)) { title() }
                    trailing?.invoke()
                }

                // Row 2: Icon + Subtitle | Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icon()
                    Box(Modifier.weight(1f)) { subtitle() }
                    expiry?.invoke()
                }
            }

            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_blue),
                    contentDescription = stringResource(R.string.accessibility_common_selected),
                    modifier = Modifier.size(theme.sizeTokens.small),
                    tint = Color.Unspecified,
                )
            }
        }

        cvvLayout?.invoke()
    }
}

@Preview(name = "Unselected", showBackground = true)
@Composable
private fun VaultedItemUnselectedPreview() = PreviewContainer {
    VaultedItem(
        isSelected = false,
        onClick = {},
        title = { Text("John Doe") },
        icon = { Icon(painterResource(R.drawable.ic_primer_credit_card), null) },
        subtitle = { Text("Visa") },
        trailing = { Text("•••• 4242") },
        expiry = { Text("12/25") },
    )
}

@Preview(name = "Selected", showBackground = true)
@Composable
private fun VaultedItemSelectedPreview() = PreviewContainer {
    VaultedItem(
        isSelected = true,
        onClick = {},
        title = { Text("John Doe") },
        icon = { Icon(painterResource(R.drawable.ic_primer_credit_card), null) },
        subtitle = { Text("Visa") },
        trailing = { Text("•••• 4242") },
        expiry = { Text("12/25") },
    )
}
