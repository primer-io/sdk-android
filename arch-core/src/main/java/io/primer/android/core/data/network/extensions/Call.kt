package io.primer.android.core.data.network.extensions

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend inline fun Call.await(): Response =
    suspendCancellableCoroutine { continuation ->
        val callback =
            object : Callback, CompletionHandler {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    if (!call.isCanceled()) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    // Close the response if cancellation won the race before delivery,
                    // otherwise the pooled connection leaks.
                    continuation.resume(response) { runCatching { response.close() } }
                }

                override fun invoke(cause: Throwable?) {
                    try {
                        cancel()
                    } catch (_: Throwable) {
                        // do nothing
                    }
                }
            }
        enqueue(callback)
        continuation.invokeOnCancellation(callback)
    }
