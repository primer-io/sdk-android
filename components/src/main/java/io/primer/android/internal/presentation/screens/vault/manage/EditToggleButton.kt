package io.primer.android.internal.presentation.screens.vault.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun EditToggleButton(
    isEditMode: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val theme = LocalPrimerTheme.current
    val textRes = if (isEditMode) R.string.primer_vault_manage_button_done else R.string.primer_vault_manage_button_edit
    val iconRes = if (isEditMode) R.drawable.ic_primer_check else R.drawable.ic_primer_edit

    TextButton(onClick = onToggle, enabled = enabled) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(iconRes), null)
            Text(
                stringResource(textRes),
                style = theme.typographyTokens.titleLarge.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
            )
        }
    }
}

@Preview(name = "Edit Mode", showBackground = true)
@Composable
private fun EditToggleButtonEditModePreview() = PreviewContainer {
    EditToggleButton(isEditMode = true, enabled = true, onToggle = {})
}

@Preview(name = "View Mode", showBackground = true)
@Composable
private fun EditToggleButtonViewModePreview() = PreviewContainer {
    EditToggleButton(isEditMode = false, enabled = true, onToggle = {})
}

@Preview(name = "Disabled", showBackground = true)
@Composable
private fun EditToggleButtonDisabledPreview() = PreviewContainer {
    EditToggleButton(isEditMode = false, enabled = false, onToggle = {})
}
