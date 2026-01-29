package io.primer.android.internal.presentation.screens.vault.selected

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.components.AppBarSpec
import io.primer.android.internal.presentation.checkout.components.LocalIsInlineFlow
import io.primer.android.internal.presentation.checkout.components.WithAppBar
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.DefaultVaultSection
import io.primer.android.internal.presentation.screens.vault.VaultViewModel

@Composable
internal fun VaultSelectedMethodScreen(
    vaultViewModel: VaultViewModel,
    checkout: CheckoutViewModel,
) {
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    var cachedFormattedAmount by remember { mutableStateOf<String?>(null) }

    // Cache formatted amount from Ready state to avoid crashes when state transitions
    LaunchedEffect(checkoutState) {
        if (checkoutState is PrimerCheckoutState.Ready) {
            val totalAmount = (checkoutState as PrimerCheckoutState.Ready).clientSession.totalAmount ?: 0
            cachedFormattedAmount = checkout.formatAmount(totalAmount)
        }
    }

    WithAppBar(
        AppBarSpec.Standard(
            title = stringResource(R.string.primer_common_button_pay_amount, cachedFormattedAmount ?: ""),
        ),
    ) {
        val theme = LocalPrimerTheme.current
        val isInlineFlow = LocalIsInlineFlow.current

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = theme.spacingTokens.large,
                end = theme.spacingTokens.large,
                bottom = theme.spacingTokens.large,
            ),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
        ) {
            item {
                Spacer(Modifier.height(theme.spacingTokens.small))
            }
            item { DefaultVaultSection(vaultViewModel = vaultViewModel) }

            // Hide "other ways to pay" in inline mode - user already has their own payment UI
            if (!isInlineFlow) {
                item { DefaultOtherPaymentMethodsButton() }
            }
        }
    }
}
