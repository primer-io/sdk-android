package io.primer.android.paymentmethods.manager.component

import io.primer.android.components.manager.core.composable.PrimerHeadlessValidatable
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessComponent
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessDataCollectable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessErrorable
import io.primer.android.paymentmethods.manager.composable.PrimerHeadlessSubmitable

interface PrimerHeadlessCollectDataComponent<T : PrimerCollectableData> :
    PrimerHeadlessComponent,
    PrimerHeadlessErrorable,
    PrimerHeadlessValidatable<T>,
    PrimerHeadlessDataCollectable<T>,
    PrimerHeadlessSubmitable
