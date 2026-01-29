package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.SectionHeader
import io.primer.android.internal.presentation.screens.vault.components.DefaultShowAllButton

@Composable
internal fun DefaultVaultSectionHeader(
    onShowAll: (() -> Unit)? = null,
) {
    SectionHeader(
        title = stringResource(R.string.primer_vault_section_title),
        modifier = Modifier.padding(bottom = LocalPrimerTheme.current.spacingTokens.medium),
        trailingContent = onShowAll?.let { { DefaultShowAllButton(onShowAll = it) } },
    )
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultVaultSectionHeaderPreview() = PreviewContainer {
    DefaultVaultSectionHeader(onShowAll = {})
}
