package io.primer.android.domain.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

internal interface Params
internal class None : Params

internal abstract class BaseFlowInteractor<out T, in P : Params> where T : Any {

    internal abstract val dispatcher: CoroutineDispatcher

    abstract fun execute(params: P): Flow<T>

    operator fun invoke(
        params: P
    ) = execute(params)
}
