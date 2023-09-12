package io.primer.sample.test

import kotlinx.coroutines.flow.Flow

interface PrimerCollectDataStep

interface PrimerCollectDataStepable<T : PrimerCollectDataStep> {

    val stepFlow: Flow<T>
}