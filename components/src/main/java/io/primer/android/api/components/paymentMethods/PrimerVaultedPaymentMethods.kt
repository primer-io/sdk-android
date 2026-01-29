package io.primer.android.api.components.paymentMethods

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.presentation.components.DefaultSubmitButton
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.DefaultVaultSectionHeader
import io.primer.android.internal.presentation.screens.vault.components.vaultItem.DefaultVaultItem
import io.primer.android.internal.presentation.utils.VaultItemProperties

/**
 * Display saved payment methods for returning customers.
 *
 * Shows the customer's saved cards/payment methods with a pay button.
 * If CVV re-entry is required, a CVV input screen is shown automatically.
 *
 * Hidden automatically if the customer has no saved payment methods.
 *
 * ## Basic usage
 * ```kotlin
 * val controller = rememberVaultedPaymentMethodsController(checkout)
 * PrimerVaultedPaymentMethods(controller)
 * ```
 *
 * ## Custom item
 * ```kotlin
 * PrimerVaultedPaymentMethods(
 *     controller = controller,
 *     item = { method, isSelected, onSelect ->
 *         MyCard(method, isSelected, onClick = onSelect)
 *     }
 * )
 * ```
 *
 * @param controller Vaulted payment methods controller from [rememberVaultedPaymentMethodsController]
 * @param modifier Modifier for the container
 * @param header Header content with "Show all" action
 * @param item Custom rendering for each saved payment method
 * @param submitButton Custom submit button
 */
@Composable
fun PrimerVaultedPaymentMethods(
    controller: PrimerVaultedPaymentMethodsController,
    modifier: Modifier = Modifier,
    header: @Composable (onShowAll: () -> Unit) -> Unit = { onShowAll ->
        VaultedPaymentMethodsDefaults.SectionHeader(onShowAll = onShowAll)
    },
    item: @Composable (
        method: PrimerVaultedPaymentMethod,
        isSelected: Boolean,
        onSelect: () -> Unit,
    ) -> Unit = { method, isSelected, onSelect ->
        VaultedPaymentMethodsDefaults.Method(method, isSelected, onSelect)
    },
    submitButton: @Composable (
        isLoading: Boolean,
        enabled: Boolean,
        onSubmit: () -> Unit,
    ) -> Unit = { isLoading, enabled, onSubmit ->
        DefaultSubmitButton(
            isLoading = isLoading,
            enabled = enabled,
            onClick = onSubmit,
        )
    },
) {
    val vaultedMethods by controller.methods.collectAsStateWithLifecycle()

    if (vaultedMethods.isEmpty()) return

    // Track which method is selected (default to first)
    var selectedMethod by remember(vaultedMethods) {
        mutableStateOf(vaultedMethods.firstOrNull())
    }
    var isLoading by remember { mutableStateOf(false) }

    val theme = LocalPrimerTheme.current
    val shape = RoundedCornerShape(theme.radiusTokens.medium)

    Column(modifier = modifier) {
        header { controller.showAll() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(theme.colorTokens().primerColorGray100)
                .padding(theme.spacingTokens.small),
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        ) {
            // Show selected method
            selectedMethod?.let { method ->
                item(method, true) { selectedMethod = method }
            }

            // Pay button - triggers payment flow (navigates to CVV screen if needed)
            submitButton(isLoading, !isLoading && selectedMethod != null) {
                selectedMethod?.let { method ->
                    isLoading = true
                    controller.select(method)
                }
            }
        }
    }
}

/**
 * Default component implementations for [PrimerVaultedPaymentMethods].
 */
object VaultedPaymentMethodsDefaults {

    /**
     * Default section header with saved card count and "Show all" button.
     */
    @Composable
    fun SectionHeader(onShowAll: () -> Unit) {
        DefaultVaultSectionHeader(onShowAll = onShowAll)
    }

    /**
     * Vaulted payment method item.
     *
     * @param method The vaulted payment method to display
     * @param isSelected Whether this item is currently selected
     * @param onSelect Called when the item is clicked to select it
     */
    @Composable
    fun Method(
        method: PrimerVaultedPaymentMethod,
        isSelected: Boolean,
        onSelect: () -> Unit,
    ) {
        DefaultVaultItem(
            properties = VaultItemProperties(
                paymentMethod = method,
                isSelected = isSelected,
                onClick = onSelect,
                cvvLayout = {},
            ),
        )
    }
}
