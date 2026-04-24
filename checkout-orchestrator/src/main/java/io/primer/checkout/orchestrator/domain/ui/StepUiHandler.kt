package io.primer.checkout.orchestrator.domain.ui

import android.content.Intent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.CoroutineScope

/**
 * Bridges a step executor's UI needs with the component's UI event flow.
 * Each step type that requires UI interaction provides a StepUiHandler.
 */
interface StepUiHandler {
    /** Observe the underlying executor's launch requests and emit ComposerUiEvents. */
    fun observeLaunchRequests(
        scope: CoroutineScope,
        paymentMethodType: String,
        emitter: suspend (ComposerUiEvent) -> Unit,
    )

    /** Route an activity result back to the executor. Returns true if handled. */
    fun handleActivityResult(
        params: PaymentMethodLauncherParams,
        resultCode: Int,
        intent: Intent?,
    ): Boolean

    /** Route an activity start event. Returns a [ComposerUiEvent] if handled, null otherwise. */
    fun handleActivityStartEvent(
        params: PaymentMethodLauncherParams,
    ): ComposerUiEvent?
}
