package io.primer.android.components.manager.nolPay.composable

import kotlinx.coroutines.flow.Flow

internal interface Stepable<T> {

    val stepFlow: Flow<T>
}
