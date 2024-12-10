package io.primer.android.components.manager.formWithRedirect.component

import io.primer.android.components.manager.formWithRedirect.composable.RedirectCollectableData
import io.primer.android.paymentmethods.manager.component.PrimerHeadlessCollectDataComponent
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStartable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable

interface PrimerHeadlessRedirectComponent<
    TCollectableData : RedirectCollectableData, TStep : PrimerHeadlessStep> :
    PrimerHeadlessCollectDataComponent<TCollectableData>,
    PrimerHeadlessSteppable<TStep>,
    PrimerHeadlessStartable {
    override fun start() { /* no-op */
    }

    override fun submit() { /* no-op */
    }

    override fun updateCollectedData(collectedData: TCollectableData) { /* no-op */
    }
}
