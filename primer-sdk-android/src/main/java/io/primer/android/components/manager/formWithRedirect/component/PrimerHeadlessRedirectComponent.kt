package io.primer.android.components.manager.formWithRedirect.component

import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.android.components.manager.core.composable.PrimerHeadlessSteppable
import io.primer.android.components.manager.formWithRedirect.composable.RedirectCollectableData

internal interface PrimerHeadlessRedirectComponent<
    TCollectableData : RedirectCollectableData, TStep : PrimerHeadlessStep> :
    PrimerHeadlessCollectDataComponent<TCollectableData>,
    PrimerHeadlessSteppable<TStep>,
    PrimerHeadlessStartable {
    override fun start() { /* no-op */ }

    override fun submit() { /* no-op */ }

    override fun updateCollectedData(collectedData: TCollectableData) { /* no-op */ }
}
