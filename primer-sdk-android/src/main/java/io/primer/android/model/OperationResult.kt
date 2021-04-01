package io.primer.android.model

sealed class OperationResult<out T> {
    data class Success<out T>(val data: T) : OperationResult<T>()
    data class Error<out T>(val error: Throwable) : OperationResult<T>()
}
