package io.primer.android.errors.extensions

import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalContracts::class)
suspend fun <R> Result<R>.onFailureWithCancellation(
    paymentMethodType: String,
    action: suspend (Throwable) -> Unit
): Result<R> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let {
        action(
            if (it is CancellationException) {
                PaymentMethodCancelledException(paymentMethodType)
            } else {
                it
            }
        )
    }
    return this
}
