package io.primer.android.core.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal fun <T> CoroutineScope.debounce(
    debounceInterval: Duration = 275.milliseconds,
    action: suspend CoroutineScope.(T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = launch {
            delay(debounceInterval)
            action(param)
        }
    }
}

internal fun <T> CoroutineScope.cancellable(
    predicate: (T) -> Boolean,
    action: suspend CoroutineScope.(T) -> Unit
): (T) -> Unit {
    var cancellableJob: Job? = null
    return { param: T ->
        if (predicate.invoke(param)) {
            cancellableJob?.cancel()
        }
        cancellableJob = launch {
            action(param)
        }
    }
}
