package io.primer.android.internal.presentation.screens.vault.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

@Composable
internal fun DefaultDeleteCancelButton(
    isLoading: Boolean,
    onCancel: () -> Unit,
) {
    val theme = LocalPrimerTheme.current

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        shape = RoundedCornerShape(theme.radiusTokens.medium),
    ) {
        Text(
            stringResource(R.string.primer_vault_delete_button_cancel),
            style = theme.typographyTokens.bodyMedium.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )
    }
}
