package io.primer.android.internal.presentation.screens.vault.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.Actions
import io.primer.android.internal.presentation.checkout.components.AppBarSpec
import io.primer.android.internal.presentation.checkout.components.WithAppBar
import io.primer.android.internal.presentation.screens.vault.VaultViewModel
import io.primer.android.internal.presentation.screens.vault.components.vaultItem.DefaultVaultItem
import io.primer.android.internal.presentation.utils.VaultItemProperties

@Composable
internal fun VaultedManageScreen(vaultViewModel: VaultViewModel) {
    val state by vaultViewModel.state.collectAsStateWithLifecycle()

    WithAppBar(
        AppBarSpec.Standard(
            title = stringResource(R.string.primer_vault_manage_title),
            actions = Actions.Custom {
                EditToggleButton(
                    isEditMode = state.isEditMode,
                    enabled = !state.isLoading,
                    onToggle = { vaultViewModel.toggleEditMode() },
                )
            },
        ),
    ) {
        VaultedManageContent(vaultViewModel)
    }
}

@Composable
private fun VaultedManageContent(vaultViewModel: VaultViewModel) {
    val state by vaultViewModel.state.collectAsStateWithLifecycle()
    val theme = LocalPrimerTheme.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = theme.spacingTokens.large, vertical = theme.spacingTokens.medium),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
    ) {
        items(state.paymentMethods, key = { it.id }) { paymentMethod ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DefaultVaultItem(
                        VaultItemProperties(
                            paymentMethod = paymentMethod,
                            isSelected = state.selectedPaymentMethod?.id == paymentMethod.id,
                            onClick = { vaultViewModel.selectForPayment(paymentMethod) },
                        ),
                    )
                }

                if (state.isEditMode) {
                    IconButton(
                        onClick = { vaultViewModel.selectForDeletion(paymentMethod) },
                        modifier = Modifier.size(theme.sizeTokens.medium),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            stringResource(R.string.accessibility_action_delete),
                            tint = theme.colorTokens().primerColorTextPrimary,
                        )
                    }
                }
            }
        }
    }
}
