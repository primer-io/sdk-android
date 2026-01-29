package io.primer.android.internal.navigation

import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Combined navigator and result handler for checkout flows.
 *
 * This class implements both [ScreenNavigator] and [CheckoutResultHandler],
 * providing a unified navigation and result handling mechanism.
 *
 * It emits navigation events that are consumed by:
 * - Sheet mode: [io.primer.android.internal.presentation.checkout.components.SheetNavHost]
 * - Inline mode: [io.primer.android.internal.presentation.checkout.components.FlowSheetOverlayInline]
 * - ViewModel: Updates checkout state based on navigation events
 */
@Suppress("TooManyFunctions")
internal class CheckoutNavigator : ScreenNavigator, CheckoutResultHandler {

    /**
     * Navigation events for UI to consume.
     */
    sealed interface NavigationEvent {
        // Screen navigation
        data object NavigateToCardForm : NavigationEvent
        data object NavigateToVaultManage : NavigationEvent
        data class ShowCvvRecapture(val vaultedMethod: PrimerVaultedPaymentMethod) : NavigationEvent
        data class StartPaymentFlow(val paymentMethodType: String) : NavigationEvent
        data class StartVaultedPaymentFlow(val vaultedMethod: PrimerVaultedPaymentMethod) : NavigationEvent

        // Loading state
        data object NavigateToLoading : NavigationEvent

        // Result navigation
        data class NavigateToSuccess(val checkoutData: PrimerCheckoutData) : NavigationEvent
        data class NavigateToError(val error: PrimerError) : NavigationEvent

        // Lifecycle
        data object Dismiss : NavigationEvent
        data object DismissAfterResult : NavigationEvent
    }

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 10)
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    // ============================================================
    // ScreenNavigator implementation
    // ============================================================

    override fun navigateToCardForm() {
        _navigationEvents.tryEmit(NavigationEvent.NavigateToCardForm)
    }

    override fun navigateToVaultManage() {
        _navigationEvents.tryEmit(NavigationEvent.NavigateToVaultManage)
    }

    override fun showCvvRecapture(vaultedMethod: PrimerVaultedPaymentMethod) {
        _navigationEvents.tryEmit(NavigationEvent.ShowCvvRecapture(vaultedMethod))
    }

    override fun startPaymentFlow(paymentMethodType: String) {
        _navigationEvents.tryEmit(NavigationEvent.StartPaymentFlow(paymentMethodType))
    }

    override fun startVaultedPaymentFlow(vaultedMethod: PrimerVaultedPaymentMethod) {
        _navigationEvents.tryEmit(NavigationEvent.StartVaultedPaymentFlow(vaultedMethod))
    }

    override fun navigateToLoading() {
        _navigationEvents.tryEmit(NavigationEvent.NavigateToLoading)
    }

    override fun dismiss() {
        _navigationEvents.tryEmit(NavigationEvent.Dismiss)
    }

    // ============================================================
    // CheckoutResultHandler implementation
    // ============================================================

    override fun onSuccess(checkoutData: PrimerCheckoutData) {
        _navigationEvents.tryEmit(NavigationEvent.NavigateToSuccess(checkoutData))
    }

    override fun onError(error: PrimerError) {
        _navigationEvents.tryEmit(NavigationEvent.NavigateToError(error))
    }

    // ============================================================
    // State management
    // ============================================================

    /**
     * Signals dismiss after the terminal state has been shown.
     * Called after success/error screen delay.
     */
    fun signalDismissAfterResult() {
        _navigationEvents.tryEmit(NavigationEvent.DismissAfterResult)
    }

    companion object {
        /** Delay before auto-dismissing after success screen is shown. */
        const val SUCCESS_SCREEN_DELAY_MS = 3000L
    }
}
