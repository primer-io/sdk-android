package io.primer.android.components.manager.core.composable

interface PrimerCollectableData

interface PrimerHeadlessDataCollectable<T : PrimerCollectableData> {

    fun updateCollectedData(t: T)
}
