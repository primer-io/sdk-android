package io.primer.android.domain.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal abstract class BaseSuspendInteractor<out T, in P : Params> where T : Any {

    internal abstract val dispatcher: CoroutineDispatcher

    suspend operator fun invoke(
        params: P
    ) = execute(params)

    protected abstract suspend fun performAction(params: P): Result<T>

    private suspend fun execute(params: P): Result<T> = withContext(dispatcher) {
        performAction(params)
    }
}
