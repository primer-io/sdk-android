package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.surcharge.paymentMethodsList
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.AllVaultedMethodsStageContent
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.VaultedCvvStageContent
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.VaultedPaymentMethodsSection
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import io.primer.android.scope.PrimerVaultedScope

/**
 * Default payment method selection screen for the Primer SDK.
 *
 * Screen structure:
 * 1. App bar with title "Pay €X.XX" and Cancel button
 * 2. Vaulted payment methods section (if available)
 * 3. "Choose payment method" heading
 * 4. Regular payment methods list
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PrimerPaymentMethodSelectionScope.DefaultPaymentMethodSelectionScreen() {
    val paymentState = state.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val vaultedScope = components.vaultedComponents.rememberScope()
    val vaultedState by vaultedScope.state.collectAsStateWithLifecycle()

    val formattedAmount = formatTitleAmount()
    val title = if (formattedAmount.isNotEmpty()) {
        context.getString(R.string.primer_components_payment_method_selection_pay_amount, formattedAmount)
    } else {
        context.getString(R.string.primer_components_payment_method_selection_pay)
    }

    Column {
        CheckoutAppBar(
            title = title,
            onCancelClick = { onCancel() },
        )
        AnimatedContent(
            targetState = vaultedState.stage,
            transitionSpec = {
                if (targetState is PrimerVaultedScope.State.Stage.Cvv) {
                    (slideInVertically { fullHeight -> fullHeight } + fadeIn()) togetherWith
                        (slideOutVertically { fullHeight -> fullHeight } + fadeOut())
                } else {
                    (slideInVertically { fullHeight -> -fullHeight } + fadeIn()) togetherWith
                        (slideOutVertically { fullHeight -> fullHeight } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
        ) { stage ->
            when (stage) {
                is PrimerVaultedScope.State.Stage.Selection -> SelectionStageContent(
                    state = paymentState,
                    vaultedScope = vaultedScope,
                    vaultedState = vaultedState,
                )

                is PrimerVaultedScope.State.Stage.Cvv -> VaultedCvvStageContent(
                    vaultedScope = vaultedScope,
                    onDismiss = { vaultedScope.cancelCvvRecapture() },
                )

                is PrimerVaultedScope.State.Stage.AllMethods -> {
                    val viewModel = components.vaultedComponents.getViewModel()
                    AllVaultedMethodsStageContent(
                        vaultedScope = vaultedScope,
                        viewModel = viewModel,
                        onDismiss = { viewModel.returnToSelection() },
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimerPaymentMethodSelectionScope.SelectionStageContent(
    state: PrimerPaymentMethodSelectionScope.State,
    vaultedScope: PrimerVaultedScope,
    vaultedState: PrimerVaultedScope.State,
) {
    val shouldShowVaultSection = vaultedState.paymentMethods.isNotEmpty() ||
        vaultedState.isLoading ||
        vaultedState.error != null

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = LocalPrimerTheme.current.spacingTokens.large,
            end = LocalPrimerTheme.current.spacingTokens.large,
            bottom = LocalPrimerTheme.current.spacingTokens.large,
        ),
        verticalArrangement = Arrangement.spacedBy(LocalPrimerTheme.current.spacingTokens.small),
    ) {
        if (shouldShowVaultSection) {
            item {
                VaultedPaymentMethodsSection(
                    vaultedState = vaultedState,
                    vaultedScope = vaultedScope,
                    vaultedComponents = components.vaultedComponents,
                )
            }
        }

        item {
            Text(
                modifier = Modifier.padding(
                    top = LocalPrimerTheme.current.spacingTokens.medium,
                    bottom = LocalPrimerTheme.current.spacingTokens.small,
                ),
                text = stringResource(R.string.primer_components_payment_method_selection_description),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            )
        }

        paymentMethodsList(state.paymentMethods, this@SelectionStageContent)
    }
}
