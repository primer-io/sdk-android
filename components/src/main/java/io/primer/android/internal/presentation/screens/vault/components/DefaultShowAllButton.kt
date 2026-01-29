package io.primer.android.internal.presentation.screens.vault.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultShowAllButton(onShowAll: () -> Unit = {}) {
    val theme = LocalPrimerTheme.current

    Row(
        modifier = Modifier.clickable(onClick = onShowAll),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xsmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.primer_vault_button_show_all),
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )
        Icon(
            painterResource(R.drawable.ic_primer_chevron_down),
            contentDescription = null,
            tint = theme.colorTokens().primerColorTextPrimary,
            modifier = Modifier.size(theme.sizeTokens.medium),
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultShowAllButtonPreview() = PreviewContainer {
    DefaultShowAllButton(onShowAll = {})
}
