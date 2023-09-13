package io.primer.android.components.manager.core

import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.composable.PrimerHeadlessComponent
import io.primer.android.components.manager.core.composable.PrimerHeadlessDataCollectable
import io.primer.android.components.manager.core.composable.PrimerHeadlessErrorable
import io.primer.android.components.manager.core.composable.PrimerHeadlessSubmitable
import io.primer.android.components.manager.core.composable.PrimerHeadlessValidatable

// session, flow

// TODO move to package

interface PrimerHeadlessCollectDataComponent<T : PrimerCollectableData> : PrimerHeadlessComponent,
    PrimerHeadlessErrorable,
    PrimerHeadlessValidatable, PrimerHeadlessDataCollectable<T>, PrimerHeadlessSubmitable
