package io.primer.android.internal.presentation.checkout.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.internal.domain.error.PrimerErrorException
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.components.Screen
import io.primer.android.internal.presentation.screens.vault.VaultViewModel
import io.primer.android.internal.presentation.screens.vault.VaultViewModelFactory
import io.primer.android.internal.presentation.screens.vault.delete.VaultDeleteConfirmationScreen
import io.primer.android.internal.presentation.screens.vault.manage.VaultedManageScreen
import io.primer.android.internal.presentation.screens.vault.selected.VaultSelectedMethodScreen

/**
 * Nested navigation graph for vault management screens.
 *
 * Contains screens for managing saved payment methods:
 * - [Screen.Vault.Manage] - List of all vaulted methods with edit/delete actions
 * - [Screen.Vault.SelectedMethod] - Selected method details
 * - [Screen.Vault.DeleteConfirmation] - Confirmation dialog before deletion
 *
 * Note: CVV recapture is handled by the top-level [Screen.CvvRecapture] route,
 * not within this graph, to allow it to be used as a startDestination in modals.
 */
internal fun NavGraphBuilder.vaultNavGraph(checkout: CheckoutViewModel) {
    navigation<Screen.Vault>(startDestination = Screen.Vault.Manage) {
        composable<Screen.Vault.SelectedMethod> {
            val vaultViewModel = rememberVaultViewModel()
            ObserveVaultNavigation(vaultViewModel, checkout)
            VaultSelectedMethodScreen(vaultViewModel, checkout)
        }

        composable<Screen.Vault.Manage> {
            val vaultViewModel = rememberVaultViewModel()
            ObserveVaultNavigation(vaultViewModel, checkout)
            VaultedManageScreen(vaultViewModel)
        }

        composable<Screen.Vault.DeleteConfirmation> {
            val vaultViewModel = rememberVaultViewModel()
            ObserveVaultNavigation(vaultViewModel, checkout)
            VaultDeleteConfirmationScreen(vaultViewModel)
        }
    }
}

@Composable
private fun rememberVaultViewModel(): VaultViewModel {
    val navController = LocalSheetNavController.current
    val viewModelStoreOwner = runCatching {
        navController.getBackStackEntry(navController.graph.id)
    }.getOrNull()

    return viewModel<VaultViewModel>(
        viewModelStoreOwner = viewModelStoreOwner ?: LocalViewModelStoreOwner.current!!,
        factory = VaultViewModelFactory(),
    )
}

@Composable
private fun ObserveVaultNavigation(
    vaultViewModel: VaultViewModel,
    checkout: CheckoutViewModel,
) {
    val navController = LocalSheetNavController.current

    LaunchedEffect(vaultViewModel) {
        vaultViewModel.navigation.collect { event ->
            when (event) {
                is VaultViewModel.NavigationEvent.NavigateToSelectedMethod -> {
                    navController.navigate(Screen.Vault.SelectedMethod)
                }
                is VaultViewModel.NavigationEvent.ShowDeleteConfirmation -> {
                    navController.navigate(Screen.Vault.DeleteConfirmation)
                }
                is VaultViewModel.NavigationEvent.ShowCvvRecapture -> {
                    navController.navigate(Screen.CvvRecapture)
                }
                is VaultViewModel.NavigationEvent.NavigateToLoading -> {
                    checkout.navigator.navigateToLoading()
                }
                is VaultViewModel.NavigationEvent.PaymentSuccess -> {
                    checkout.navigator.onSuccess(event.checkoutData)
                }
                is VaultViewModel.NavigationEvent.PaymentError -> {
                    val primerError = (event.error as? PrimerErrorException)?.primerError
                        ?: PrimerUnknownError(event.error.message ?: "Payment failed")
                    checkout.navigator.onError(primerError)
                }
                is VaultViewModel.NavigationEvent.DeleteSuccess -> {
                    navController.popBackStack()
                }
            }
        }
    }
}
