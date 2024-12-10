package io.primer.android.components.manager.componentWithRedirect.component

import io.primer.android.paymentmethods.manager.component.PrimerHeadlessCollectDataComponent
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStartable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable

interface PrimerHeadlessMainComponent<TCollectableData : PrimerCollectableData,
    TStep : PrimerHeadlessStep> :
    PrimerHeadlessCollectDataComponent<TCollectableData>,
    PrimerHeadlessSteppable<TStep>,
    PrimerHeadlessStartable
