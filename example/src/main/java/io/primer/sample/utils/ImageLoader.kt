package io.primer.sample.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.IOException
import java.util.WeakHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class ImageLoader constructor(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
) {
    private val jobs = WeakHashMap<ImageView, Job>()

    private val scope =
        CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadImage(url: String, target: ImageView) {
        jobs[target] = scope.launch {
            runCatching { loadImage(url) }
                .onSuccess { target.setImageBitmap(it) }
                .onFailure { clear(target) }
        }
    }

    private fun clear(target: ImageView) = jobs.remove(target)?.cancel()

    fun clearAll() {
        jobs.clear()
        scope.cancel()
    }

    private suspend fun loadImage(url: String) = withContext<Bitmap>(Dispatchers.IO) {
        val response: Response = okHttpClient
            .newCall(Request.Builder().url(url).get().build())
            .await()

        val bufferedInputStream = BufferedInputStream(response.body?.byteStream())
        bufferedInputStream.use {
            BitmapFactory.decodeStream(bufferedInputStream)
        }
    }

    private suspend inline fun Call.await(): Response =
        suspendCancellableCoroutine { continuation ->
            val callback = object : Callback, CompletionHandler {
                override fun onFailure(call: Call, e: IOException) {
                    if (!call.isCanceled()) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun invoke(cause: Throwable?) {
                    runCatching { cancel() }
                }
            }
            enqueue(callback)
            continuation.invokeOnCancellation(callback)
        }
}


