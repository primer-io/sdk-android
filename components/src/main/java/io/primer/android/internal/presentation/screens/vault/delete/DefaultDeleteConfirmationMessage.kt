package io.primer.android.internal.presentation.screens.vault.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultDeleteConfirmationMessage() {
    val theme = LocalPrimerTheme.current

    Text(
        stringResource(R.string.primer_vault_delete_message),
        style = theme.typographyTokens.bodyMedium.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultDeleteConfirmationMessagePreview() = PreviewContainer {
    DefaultDeleteConfirmationMessage()
}
