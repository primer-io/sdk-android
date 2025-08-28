package io.primer.android.core.extensions

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

val DEFAULT_DEBOUNCE_INTERVAL_IN_MILLIS: Duration = 275.milliseconds

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> CoroutineScope.debounce(
    debounceInterval: Duration = DEFAULT_DEBOUNCE_INTERVAL_IN_MILLIS,
    action: suspend CoroutineScope.(T) -> Unit,
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob =
            launch {
                delay(debounceInterval)
                action(param)
            }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal fun <T> CoroutineScope.cancellable(
    predicate: (T) -> Boolean,
    action: suspend CoroutineScope.(T) -> Unit,
): (T) -> Unit {
    var cancellableJob: Job? = null
    return { param: T ->
        if (predicate.invoke(param)) {
            cancellableJob?.cancel()
        }
        cancellableJob =
            launch {
                action(param)
            }
    }
}
