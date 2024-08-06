package io.primer.android.components.manager.ach

import io.primer.android.components.manager.core.component.PrimerHeadlessCollectDataComponent
import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.composable.PrimerHeadlessStartable
import io.primer.android.components.manager.core.composable.PrimerHeadlessStep
import io.primer.android.components.manager.core.composable.PrimerHeadlessSteppable

interface PrimerHeadlessAchComponent<TCollectableData : PrimerCollectableData,
    TStep : PrimerHeadlessStep> :
    PrimerHeadlessCollectDataComponent<TCollectableData>,
    PrimerHeadlessSteppable<TStep>,
    PrimerHeadlessStartable
