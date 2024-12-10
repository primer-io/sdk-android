package io.primer.android.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

interface Params
object None : Params

abstract class BaseFlowInteractor<out T, in P : Params> where T : Any {

    protected abstract val dispatcher: CoroutineDispatcher

    abstract fun execute(params: P): Flow<T>

    operator fun invoke(
        params: P
    ) = execute(params)
}
