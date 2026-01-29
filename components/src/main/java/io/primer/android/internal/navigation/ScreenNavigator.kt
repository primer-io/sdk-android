package io.primer.android.internal.navigation

import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

/**
 * Handles navigation between screens in the checkout flow.
 */
internal interface ScreenNavigator {
    /** Navigate to card form screen. */
    fun navigateToCardForm()

    /** Navigate to vault management screen. */
    fun navigateToVaultManage()

    /** Show CVV recapture for a vaulted payment method. */
    fun showCvvRecapture(vaultedMethod: PrimerVaultedPaymentMethod)

    /** Start payment flow for a specific payment method type. */
    fun startPaymentFlow(paymentMethodType: String)

    /** Start vaulted payment flow. */
    fun startVaultedPaymentFlow(vaultedMethod: PrimerVaultedPaymentMethod)

    /** Navigate to loading screen. */
    fun navigateToLoading()

    /** Dismiss the checkout UI entirely. */
    fun dismiss()
}
