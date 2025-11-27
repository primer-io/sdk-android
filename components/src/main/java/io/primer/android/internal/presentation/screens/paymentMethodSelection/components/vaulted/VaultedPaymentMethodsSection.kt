package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.PrimerVaultedComponents
import io.primer.android.components.R
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.scope.PrimerVaultedScope
import kotlinx.coroutines.launch

/**
 * Main composable for the vaulted payment methods section.
 * Handles all vault states and user interactions.
 */
@Suppress("LongMethod")
@Composable
internal fun VaultedPaymentMethodsSection(
    vaultedState: PrimerVaultedScope.State,
    vaultedScope: PrimerVaultedScope,
    vaultedComponents: PrimerVaultedComponents,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val scopedVaultedMethods = vaultedState.paymentMethods
    val selectedPaymentMethodId = vaultedState.selectedPaymentMethodId

    // Auto-select first payment method when vaulted methods are loaded
    LaunchedEffect(scopedVaultedMethods, selectedPaymentMethodId) {
        if (scopedVaultedMethods.isNotEmpty() && selectedPaymentMethodId == null) {
            vaultedScope.selectPaymentMethod(scopedVaultedMethods.first().id)
        }
    }

    // Don't show the section if no vaulted methods and not loading
    val isVaultLoading = vaultedState.isLoading
    val isVaultError = vaultedState.error != null

    if (scopedVaultedMethods.isEmpty() && !isVaultLoading && !isVaultError) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalPrimerTheme.current.spacingTokens.medium),
    ) {
        VaultSectionHeader(
            onShowAllClick = { vaultedScope.showAllMethods() },
        )

        // Content (loading, error, or list with pay button in gray container)
        when {
            isVaultLoading -> {
                vaultedComponents.loadingState(Modifier.fillMaxWidth())
            }
            isVaultError -> {
                vaultedComponents.errorState(
                    vaultedState.error,
                    { /* Retry action intentionally left for host app */ },
                    Modifier.fillMaxWidth(),
                )
            }
            scopedVaultedMethods.isNotEmpty() -> {
                // Gray background container for payment method and Pay button
                VaultedHighlightedContainer(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val isProcessing = vaultedState.isProcessing
                    val isPayButtonEnabled = selectedPaymentMethodId != null && !isProcessing
                    val shouldShowPayButton = !vaultedState.isCvvRequired

                    VaultedPaymentMethodsList(
                        vaultedScope = vaultedScope,
                        vaultedPaymentMethods = scopedVaultedMethods,
                        selectedPaymentMethodId = selectedPaymentMethodId,
                        vaultedComponents = vaultedComponents,
                    )

                    if (shouldShowPayButton) {
                        vaultedState.error?.let { throwable ->
                            val message = throwable.message
                                ?: stringResource(R.string.primer_components_vault_error)
                            Text(
                                text = message,
                                style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
                                color = LocalPrimerTheme.current.colorTokens().primerColorTextNegative,
                            )
                            Spacer(modifier = Modifier.height(LocalPrimerTheme.current.spacingTokens.small))
                        }

                        vaultedComponents.payButton(
                            isPayButtonEnabled,
                            isProcessing,
                        ) {
                            coroutineScope.launch {
                                vaultedScope.submit()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultedPaymentMethodsList(
    vaultedScope: PrimerVaultedScope,
    vaultedPaymentMethods: List<PrimerVaultedPaymentMethod>,
    selectedPaymentMethodId: String?,
    vaultedComponents: PrimerVaultedComponents,
) {
    val paymentMethodToShow = vaultedPaymentMethods.find { it.id == selectedPaymentMethodId }
        ?: vaultedPaymentMethods.firstOrNull()

    if (paymentMethodToShow != null) {
        val theme = LocalPrimerTheme.current
        val isSelected = paymentMethodToShow.id == selectedPaymentMethodId
        val borderColor = if (isSelected) {
            theme.colorTokens().primerColorBorderOutlinedSelected
        } else {
            theme.colorTokens().primerColorBorderOutlinedDefault
        }
        val backgroundColor = theme.colorTokens().primerColorBackground
        val borderWidth = if (isSelected) {
            theme.borderWidthTokens.medium
        } else {
            theme.borderWidthTokens.thin
        }
        val shape = RoundedCornerShape(theme.radiusTokens.medium)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .border(borderWidth, borderColor, shape)
                    .background(backgroundColor),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(theme.spacingTokens.medium),
                ) {
                    vaultedComponents.vaultedPaymentMethod(
                        paymentMethodToShow,
                        isSelected,
                        Modifier.fillMaxWidth(),
                    ) {
                        // Toggle selection on click
                        if (paymentMethodToShow.id == selectedPaymentMethodId) {
                            vaultedScope.clearSelection()
                        } else {
                            vaultedScope.selectPaymentMethod(paymentMethodToShow.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DefaultVaultLoadingState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalPrimerTheme.current.spacingTokens.large),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(LocalPrimerTheme.current.spacingTokens.medium),
            color = LocalPrimerTheme.current.colorTokens().primerColorBrand,
        )
    }
}

/**
 * Error state composable shown when vault operations fail.
 * Displays a generic error message and optional retry action.
 *
 * @param onRetry Optional callback for retrying the failed operation
 * @param modifier Modifier for styling the error state
 */
@Composable
internal fun DefaultVaultErrorState(
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalPrimerTheme.current.spacingTokens.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalPrimerTheme.current.spacingTokens.small),
    ) {
        Text(
            text = stringResource(R.string.primer_components_vault_error),
            style = LocalPrimerTheme.current.typographyTokens.bodyMedium.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextNegative,
            fontWeight = FontWeight.Medium,
        )

        if (onRetry != null) {
            Text(
                text = stringResource(R.string.primer_components_retry),
                style = LocalPrimerTheme.current.typographyTokens.bodySmall.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextSecondary,
            )
        }
    }
}

@Composable
private fun VaultSectionHeader(
    onShowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.primer_components_saved_payment_methods),
            style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
            color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
        )

        Row(
            modifier = Modifier.clickable(onClick = onShowAllClick),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.primer_components_vaulted_show_all),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_chevron_down),
                contentDescription = null,
                tint = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
