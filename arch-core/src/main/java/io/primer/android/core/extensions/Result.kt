package io.primer.android.core.extensions

import androidx.annotation.RestrictTo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <T, R> T.runSuspendCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (expected: CancellationException) {
        throw expected
    } catch (expected: Throwable) {
        Result.failure(expected)
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <T, R> Result<T>.mapSuspendCatching(transform: (value: T) -> R): Result<R> {
    val successResult = getOrNull()
    return when {
        successResult != null -> runSuspendCatching { transform(successResult) }
        else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <T, R> Result<T>.flatMap(block: (T) -> (Result<R>)): Result<R> {
    return this.mapSuspendCatching {
        block(it).getOrThrow()
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
suspend inline fun <T1, T2, R> Result<T1>.zipWith(
    other: Result<T2>,
    crossinline transform: suspend (T1, T2) -> R,
): Result<R> =
    coroutineScope {
        val deferredT1 = async { this@zipWith }
        val deferredT2 = async { other }
        deferredT1.await().flatMap { t1 ->
            deferredT2.await().map { t2 ->
                transform(t1, t2)
            }
        }
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <R> Result<R>.onError(action: (Throwable) -> Unit): Result<R> {
    return try {
        exceptionOrNull()?.let { throwable ->
            action(throwable)
            this
        } ?: this
    } catch (expected: Exception) {
        Result.failure(expected)
    }
}
