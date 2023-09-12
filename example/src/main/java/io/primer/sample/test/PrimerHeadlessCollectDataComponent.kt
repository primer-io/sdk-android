package io.primer.sample.test

import io.primer.sample.PrimerHeadlessErrorable

// session, flow

interface PrimerHeadlessCollectDataComponent<T : PrimerCollectableData> : PrimerHeadlessComponent,
    PrimerHeadlessErrorable,
    PrimerHeadlessValidatable, PrimerHeadlessDataCollectable<T>, PrimerHeadlessSubmitable