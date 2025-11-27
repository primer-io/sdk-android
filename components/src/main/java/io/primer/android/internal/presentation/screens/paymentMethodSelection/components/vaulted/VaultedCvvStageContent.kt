@file:Suppress("UnusedPrivateMember")

package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import io.primer.android.scope.PrimerVaultedScope
import kotlinx.coroutines.launch

private const val CARD_LAST_DIGITS_LENGTH = 4

/**
 * Content displayed when CVV recapture is required for a vaulted payment method.
 *
 * This screen shows:
 * - The selected vaulted payment method (locked, non-clickable)
 * - CVV input field
 * - Pay button
 * - "Show other ways to pay" button to return to selection
 */
@Suppress("LongMethod")
@Composable
internal fun PrimerPaymentMethodSelectionScope.VaultedCvvStageContent(
    vaultedScope: PrimerVaultedScope,
    onDismiss: () -> Unit,
) {
    val theme = LocalPrimerTheme.current
    val coroutineScope = rememberCoroutineScope()
    val state by vaultedScope.state.collectAsStateWithLifecycle()
    val vaultedComponents = components.vaultedComponents

    var showCvvError by remember(state.selectedPaymentMethodId) { mutableStateOf(false) }

    val selectedPaymentMethod = state.paymentMethods.find { it.id == state.selectedPaymentMethodId }
        ?: state.paymentMethods.firstOrNull()
    val isPayButtonEnabled = !state.isProcessing && state.isCvvValid

    LaunchedEffect(state.isCvvRequired, state.isProcessing) {
        if (!state.isCvvRequired && !state.isProcessing) {
            showCvvError = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = theme.spacingTokens.large,
                end = theme.spacingTokens.large,
                top = theme.spacingTokens.large,
                bottom = theme.spacingTokens.large,
            ),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
    ) {
        Text(
            text = stringResource(R.string.primer_components_vaulted_payment_selection_description),
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )

        selectedPaymentMethod?.let { method ->
            val borderColor = theme.colorTokens().primerColorBorderOutlinedSelected
            val shape = RoundedCornerShape(theme.radiusTokens.medium)

            VaultedHighlightedContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .border(
                            width = theme.borderWidthTokens.medium,
                            color = borderColor,
                            shape = shape,
                        )
                        .background(theme.colorTokens().primerColorBackground)
                        .padding(theme.spacingTokens.medium),
                    verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
                ) {
                    vaultedComponents.vaultedPaymentMethod(
                        method,
                        true,
                        Modifier.fillMaxWidth(),
                    ) { }

                    VaultedCvvRecaptureItem(
                        cvvValue = state.cvvValue,
                        onCvvChange = { value ->
                            vaultedScope.updateCvv(value)
                            showCvvError = false
                        },
                        showError = showCvvError,
                        enabled = !state.isProcessing,
                        cvvLength = state.expectedCvvLength,
                    )
                }

                state.error?.let { error ->
                    Text(
                        text = error.message
                            ?: stringResource(R.string.primer_components_vault_cvv_generic_error),
                        style = theme.typographyTokens.bodySmall.toTextStyle(),
                        color = theme.colorTokens().primerColorTextNegative,
                    )
                }

                vaultedComponents.payButton(isPayButtonEnabled, state.isProcessing) {
                    if (state.isCvvValid) {
                        coroutineScope.launch {
                            vaultedScope.cvvRecapture()
                        }
                    } else {
                        showCvvError = true
                    }
                }
            }
        }

        PrimerButton(
            onClick = {
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            borderColor = theme.colorTokens().primerColorBorderOutlinedDefault,
            backgroundColor = theme.colorTokens().primerColorBackground,
        ) {
            Text(
                text = stringResource(R.string.primer_components_vault_show_other_ways_to_pay),
                style = theme.typographyTokens.titleLarge.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
            )
        }
    }
}

@Preview(showBackground = true, name = "Vaulted CVV Stage")
@Composable
private fun VaultedCvvStageContentPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        MaterialTheme {
            val previewMethods = remember { createPreviewVaultedPaymentMethods() }
            val selectedMethod = remember(previewMethods) { previewMethods.first() }
            val theme = LocalPrimerTheme.current

            Column {
                CheckoutAppBar(
                    title = "Pay $99.00",
                    onCancelClick = {},
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(theme.spacingTokens.large),
                    verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
                ) {
                    Text(
                        text = stringResource(R.string.primer_components_vaulted_payment_selection_description),
                        style = theme.typographyTokens.titleLarge.toTextStyle(),
                        color = theme.colorTokens().primerColorTextPrimary,
                    )

                    val borderColor = theme.colorTokens().primerColorBorderOutlinedSelected
                    val shape = RoundedCornerShape(theme.radiusTokens.medium)

                    VaultedHighlightedContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .border(
                                    width = theme.borderWidthTokens.medium,
                                    color = borderColor,
                                    shape = shape,
                                )
                                .background(theme.colorTokens().primerColorBackground)
                                .padding(theme.spacingTokens.medium),
                            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
                        ) {
                            VaultedCardPaymentMethodItem(
                                paymentMethod = selectedMethod,
                                isSelected = true,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                            )

                            VaultedCvvRecaptureItem(
                                cvvValue = "",
                                onCvvChange = {},
                                showError = false,
                                enabled = true,
                                cvvLength = 3,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        VaultedSubmitButton(
                            enabled = false,
                            isLoading = false,
                            onClick = {},
                        )
                    }

                    PrimerButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = theme.colorTokens().primerColorBorderOutlinedDefault,
                        backgroundColor = theme.colorTokens().primerColorBackground,
                    ) {
                        Text(
                            text = stringResource(R.string.primer_components_vault_show_other_ways_to_pay),
                            style = theme.typographyTokens.titleLarge.toTextStyle(),
                            color = theme.colorTokens().primerColorTextPrimary,
                        )
                    }
                }
            }
        }
    }
}
