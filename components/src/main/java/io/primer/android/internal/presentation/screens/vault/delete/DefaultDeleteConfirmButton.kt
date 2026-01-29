package io.primer.android.internal.presentation.screens.vault.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

@Composable
internal fun DefaultDeleteConfirmButton(
    isLoading: Boolean,
    onConfirm: () -> Unit,
) {
    val theme = LocalPrimerTheme.current

    Button(
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        shape = RoundedCornerShape(theme.radiusTokens.medium),
        colors = ButtonDefaults.buttonColors(containerColor = theme.colorTokens().primerColorBrand),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                Modifier.size(theme.sizeTokens.medium),
                Color.White,
                strokeWidth = theme.borderWidthTokens.medium,
            )
        } else {
            Text(
                stringResource(R.string.primer_vault_delete_button_confirm),
                style = theme.typographyTokens.bodyMedium.toTextStyle(),
            )
        }
    }
}
