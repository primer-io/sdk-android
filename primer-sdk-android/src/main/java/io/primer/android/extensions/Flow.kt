package io.primer.android.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal fun <T> Flow<T>.doOnError(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    onError: (Throwable) -> Unit
): Flow<T> {
    return flow {
        try {
            collect { value ->
                emit(value)
            }
        } catch (expected: Exception) {
            onError(expected)
            throw expected
        }
    }.flowOn(dispatcher)
}
