package io.primer.android.components.manager.redirect.composable

import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep

sealed interface WebRedirectStep : PrimerHeadlessStep {
    data object Loading : WebRedirectStep // TODO never emitted, perhaps remove

    data object Loaded : WebRedirectStep // TODO never emitted, perhaps remove

    data object Dismissed : WebRedirectStep

    data object Success : WebRedirectStep
}
