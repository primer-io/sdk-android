package io.primer.android.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

fun <T> Flow<T>.collectIn(list: MutableList<T>, coroutineScope: CoroutineScope): Job =
    onEach { list += it }.launchIn(coroutineScope)
