package io.primer.jscore.infrastructure.core

import android.content.Context
import androidx.javascriptengine.JavaScriptSandbox
import androidx.javascriptengine.SandboxUnsupportedException
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class JavaScriptSandboxProvider(private val context: Context) {

    private val mutex = Mutex()
    private var sandbox: JavaScriptSandbox? = null

    suspend fun get(): JavaScriptSandbox = mutex.withLock {
        sandbox ?: createSandbox().also { sandbox = it }
    }

    fun close() {
        sandbox?.close()
        sandbox = null
    }

    private suspend fun createSandbox(): JavaScriptSandbox {
        if (JavaScriptSandbox.isSupported().not()) {
            throw SandboxUnsupportedException("JS Sandbox not supported on this device.")
        }
        return JavaScriptSandbox.createConnectedInstanceAsync(context).await()
    }

    private suspend fun <T> ListenableFuture<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addListener({
                try {
                    continuation.resume(get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, MoreExecutors.directExecutor())

            continuation.invokeOnCancellation {
                cancel(true)
            }
        }
}
