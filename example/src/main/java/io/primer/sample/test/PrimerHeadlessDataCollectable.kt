package io.primer.sample.test

interface PrimerCollectableData


interface PrimerHeadlessDataCollectable<T : PrimerCollectableData> {

    fun updateCollectedData(t: T)
}