package io.primer.android.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.presentation.screens.paymentMethodSelection.VaultedPaymentMethodSelectionViewModel
import io.primer.android.internal.presentation.screens.paymentMethodSelection.VaultedPaymentMethodSelectionViewModelFactory
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.DefaultVaultErrorState
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.DefaultVaultLoadingState
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.VaultedCardPaymentMethodItem
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted.VaultedSubmitButton
import io.primer.android.scope.PrimerVaultedScope

/**
 * Components for customizing the vaulted payment methods UI.
 * Provides customizable composables for the vault section, individual payment methods, and pay button.
 */
class PrimerVaultedComponents : DISdkComponent {

    /**
     * Creates and remembers a PrimerVaultedScope instance.
     * This scope manages the state and operations for vaulted payment methods.
     *
     * @return A PrimerVaultedScope that survives configuration changes
     */
    @Composable
    fun rememberScope(): PrimerVaultedScope {
        return viewModel<VaultedPaymentMethodSelectionViewModel>(
            factory = resolve<VaultedPaymentMethodSelectionViewModelFactory>(),
        )
    }

    /**
     * Gets the ViewModel instance for direct access to methods not exposed in PrimerVaultedScope.
     * Used internally for stage management operations.
     *
     * @return The VaultedPaymentMethodSelectionViewModel instance
     */
    @Composable
    internal fun getViewModel(): VaultedPaymentMethodSelectionViewModel {
        return viewModel<VaultedPaymentMethodSelectionViewModel>(
            factory = resolve<VaultedPaymentMethodSelectionViewModelFactory>(),
        )
    }

    /**
     * Pay button composable for submitting the selected vaulted payment method.
     * Displays with proper enabled/disabled states and loading indicators.
     *
     * @param enabled Whether the button should be enabled for interaction
     * @param isLoading Whether the button should show a loading state
     * @param onClick Callback invoked when the button is clicked
     */
    var payButton: @Composable (
        enabled: Boolean,
        isLoading: Boolean,
        onClick: () -> Unit,
    ) -> Unit = { enabled, isLoading, onClick ->
        VaultedSubmitButton(
            enabled = enabled,
            isLoading = isLoading,
            onClick = onClick,
        )
    }

    /**
     * Individual vaulted payment method row composable.
     * Displays payment method information with selection state.
     *
     * @param paymentMethod The vaulted payment method to display
     * @param isSelected Whether this payment method is currently selected
     * @param modifier Modifier for styling the payment method row
     * @param onClick Callback invoked when the payment method is selected
     */
    var vaultedPaymentMethod: @Composable (
        paymentMethod: PrimerVaultedPaymentMethod,
        isSelected: Boolean,
        modifier: Modifier,
        onClick: () -> Unit,
    ) -> Unit = { paymentMethod, isSelected, modifier, onClick ->
        VaultedCardPaymentMethodItem(
            paymentMethod = paymentMethod,
            isSelected = isSelected,
            modifier = modifier,
            onClick = onClick,
        )
    }

    /**
     * Error state composable shown when vault operations fail.
     * Provides error context for custom implementations and shows a generic fallback by default.
     *
     * @param error The error that occurred
     * @param onRetry Optional callback for retrying the failed operation
     * @param modifier Modifier for styling the error state
     */
    var errorState: @Composable (
        error: Throwable,
        onRetry: (() -> Unit)?,
        modifier: Modifier,
    ) -> Unit = { _, onRetry, modifier ->
        DefaultVaultErrorState(onRetry = onRetry, modifier = modifier)
    }

    /**
     * Loading state composable shown while vaulted payment methods are being fetched.
     *
     * @param modifier Modifier for styling the loading state
     */
    var loadingState: @Composable (modifier: Modifier) -> Unit = { modifier ->
        DefaultVaultLoadingState(modifier = modifier)
    }
}
