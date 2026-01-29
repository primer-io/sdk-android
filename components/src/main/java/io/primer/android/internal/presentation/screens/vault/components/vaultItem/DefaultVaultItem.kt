package io.primer.android.internal.presentation.screens.vault.components.vaultItem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.internal.presentation.preview.PreviewMocks
import io.primer.android.internal.presentation.utils.VaultItemProperties

@Composable
internal fun DefaultVaultItem(properties: VaultItemProperties) {
    val theme = LocalPrimerTheme.current
    val info = rememberInfo(properties.paymentMethod)

    VaultedItem(
        isSelected = properties.isSelected,
        onClick = properties.onClick,
        title = {
            Text(
                text = info.title,
                style = theme.typographyTokens.bodyMedium.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
                fontWeight = FontWeight.Medium,
            )
        },
        icon = info.icon,
        subtitle = {
            Text(
                text = info.subtitle,
                style = theme.typographyTokens.bodySmall.toTextStyle(),
                color = theme.colorTokens().primerColorTextSecondary,
            )
        },
        trailing = info.trailingText?.let { text ->
            {
                Text(
                    text = text,
                    style = theme.typographyTokens.bodyMedium.toTextStyle(),
                    color = theme.colorTokens().primerColorTextPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        expiry = info.expiryText?.let { text ->
            {
                Text(
                    text = text,
                    style = theme.typographyTokens.bodySmall.toTextStyle(),
                    color = theme.colorTokens().primerColorTextSecondary,
                )
            }
        },
        cvvLayout = properties.cvvLayout,
    )
}

@Preview(name = "Unselected", showBackground = true)
@Composable
private fun DefaultVaultItemUnselectedPreview() = PreviewContainer {
    DefaultVaultItem(
        properties = VaultItemProperties(
            paymentMethod = PreviewMocks.vaultedPaymentMethod,
            isSelected = false,
        ),
    )
}

@Preview(name = "Selected", showBackground = true)
@Composable
private fun DefaultVaultItemSelectedPreview() = PreviewContainer {
    DefaultVaultItem(
        properties = VaultItemProperties(
            paymentMethod = PreviewMocks.vaultedPaymentMethod,
            isSelected = true,
        ),
    )
}
