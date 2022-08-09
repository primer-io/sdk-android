package io.primer.android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import io.primer.android.model.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.util.WeakHashMap

internal class ImageLoader constructor(private val okHttpClient: OkHttpClient) {

    private val jobs = WeakHashMap<ImageView, Job>()
    private val scope =
        CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadImage(url: String, target: ImageView) = loadImage(url, null, target)

    fun loadImage(url: String, placeholder: Drawable?, target: ImageView) {
        target.setImageDrawable(placeholder)
        jobs[target] = scope.launch {
            try {
                target.setImageBitmap(loadImage(url))
            } catch (_: Exception) {
                clear(target)
            }
        }
    }

    fun clear(target: ImageView) = jobs.remove(target)?.cancel()

    fun clearAll() {
        jobs.clear()
        scope.cancel()
    }

    private suspend fun loadImage(url: String) = withContext<Bitmap>(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response: Response = okHttpClient
            .newCall(request)
            .await()

        val bufferedInputStream = BufferedInputStream(response.body()?.byteStream())
        bufferedInputStream.use {
            BitmapFactory.decodeStream(bufferedInputStream)
        }
    }
}
