package io.primer.android.extensions

import kotlin.coroutines.cancellation.CancellationException

internal inline fun <T, R> T.runSuspendCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (expected: Exception) {
        Result.failure(expected)
    }
}

internal inline fun <T, R> Result<T>.mapSuspendCatching(transform: (value: T) -> R): Result<R> {
    val successResult = getOrNull()
    return when {
        successResult != null -> runSuspendCatching { transform(successResult) }
        else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
    }
}

internal inline fun <R> Result<R>.onError(transform: (Throwable) -> Throwable): Result<R> {
    return try {
        exceptionOrNull()?.let { throwable ->
            Result.failure(transform(throwable))
        } ?: this
    } catch (expected: Exception) {
        Result.failure(expected)
    }
}
