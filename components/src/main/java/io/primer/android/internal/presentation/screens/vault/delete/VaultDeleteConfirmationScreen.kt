package io.primer.android.internal.presentation.screens.vault.delete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.AppBarSpec
import io.primer.android.internal.presentation.checkout.components.WithAppBar
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController
import io.primer.android.internal.presentation.screens.vault.VaultViewModel
import io.primer.android.internal.presentation.screens.vault.components.vaultItem.DefaultVaultItem
import io.primer.android.internal.presentation.utils.VaultItemProperties

@Composable
internal fun VaultDeleteConfirmationScreen(vaultViewModel: VaultViewModel) {
    WithAppBar(
        AppBarSpec.Standard(
            title = stringResource(R.string.primer_vault_manage_title),
        ),
    ) {
        VaultDeleteConfirmationContent(vaultViewModel)
    }
}

@Composable
private fun VaultDeleteConfirmationContent(vaultViewModel: VaultViewModel) {
    val navController = LocalSheetNavController.current
    val state by vaultViewModel.state.collectAsStateWithLifecycle()

    val paymentMethod = checkNotNull(state.selectedPaymentMethod) {
        "selectedPaymentMethod must not be null in DeleteConfirmation screen"
    }

    val theme = LocalPrimerTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(theme.spacingTokens.large),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.large),
    ) {
        DefaultVaultItem(
            VaultItemProperties(paymentMethod, isSelected = false),
        )

        DefaultDeleteConfirmationMessage()

        state.error?.let { error ->
            Text(
                text = error.message ?: stringResource(R.string.primer_common_error_generic),
                style = theme.typographyTokens.bodyMedium.toTextStyle(),
                color = theme.colorTokens().primerColorTextNegative,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DefaultDeleteCancelButton(
                    isLoading = state.isLoading,
                    onCancel = { navController.popBackStack() },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DefaultDeleteConfirmButton(
                    isLoading = state.isLoading,
                    onConfirm = { vaultViewModel.delete() },
                )
            }
        }
    }
}
