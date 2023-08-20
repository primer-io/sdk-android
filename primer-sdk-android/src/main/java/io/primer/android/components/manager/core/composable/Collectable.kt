package io.primer.android.components.manager.core.composable

import kotlinx.coroutines.flow.Flow

internal interface Collectable<T> {

    val collectedData: Flow<T>

    fun updateCollectedData(t: T)
}
