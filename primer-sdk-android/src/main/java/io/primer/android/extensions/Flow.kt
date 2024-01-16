package io.primer.android.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration

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

suspend fun <T> Flow<T>.toListDuring(
    duration: Duration
): List<T> = coroutineScope {
    val result = mutableListOf<T>()
    val job = launch {
        this@toListDuring.collect(result::add)
    }
    delay(duration)
    job.cancel()
    return@coroutineScope result
}

fun <T> Flow<T>.collectIn(list: MutableList<T>, coroutineScope: CoroutineScope): Job =
    onEach { list += it }.launchIn(coroutineScope)
