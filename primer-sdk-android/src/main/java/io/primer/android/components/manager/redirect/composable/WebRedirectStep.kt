package io.primer.android.components.manager.redirect.composable

import io.primer.android.components.manager.core.composable.PrimerHeadlessStep

internal sealed interface WebRedirectStep : PrimerHeadlessStep {
    object Loading : WebRedirectStep
    object Loaded : WebRedirectStep
    object Dismissed : WebRedirectStep
    object Success : WebRedirectStep
}
