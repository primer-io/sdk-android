package io.primer.android.paymentmethods.manager.component.ach

import io.primer.android.paymentmethods.manager.component.PrimerHeadlessCollectDataComponent
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStartable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessStep
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSteppable

interface PrimerHeadlessAchComponent<TCollectableData : PrimerCollectableData,
    TStep : PrimerHeadlessStep> :
    PrimerHeadlessCollectDataComponent<TCollectableData>,
    PrimerHeadlessSteppable<TStep>,
    PrimerHeadlessStartable
