package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.presentation.checkout.components.Screen
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController
import io.primer.android.internal.presentation.components.DefaultSubmitButton
import io.primer.android.internal.presentation.screens.vault.VaultViewModel
import io.primer.android.internal.presentation.screens.vault.components.vaultItem.DefaultVaultItem
import io.primer.android.internal.presentation.utils.VaultItemProperties

@Composable
internal fun DefaultVaultSection(
    vaultViewModel: VaultViewModel,
    cvvLayout: (@Composable () -> Unit)? = null,
) {
    val state by vaultViewModel.state.collectAsStateWithLifecycle()
    val selectedPaymentMethod = state.selectedPaymentMethod ?: return
    val navController = LocalSheetNavController.current

    val theme = LocalPrimerTheme.current
    val shape = RoundedCornerShape(theme.radiusTokens.medium)

    Column {
        DefaultVaultSectionHeader(
            onShowAll = {
                navController.navigate(Screen.Vault.Manage) {
                    popUpTo(Screen.Vault.Manage) { inclusive = true }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(theme.colorTokens().primerColorGray100)
                .padding(theme.spacingTokens.small),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        ) {
            DefaultVaultItem(
                VaultItemProperties(
                    paymentMethod = selectedPaymentMethod,
                    isSelected = true,
                    cvvLayout = cvvLayout ?: {},
                ),
            )
            DefaultSubmitButton(
                isLoading = state.isLoading,
                enabled = !state.isLoading,
                onClick = { vaultViewModel.submit() },
            )
        }
    }
}
