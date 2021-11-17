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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.util.WeakHashMap

internal class ImageLoader constructor(private val okHttpClient: OkHttpClient) {

    private val jobs = WeakHashMap<ImageView, Job>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadImage(url: String, placeholder: Drawable, target: ImageView) {
        target.setImageDrawable(placeholder)
        jobs[target] = scope.launch {
            loadImage(url)
                .catch { clear(target) }
                .collect {
                    target.setImageBitmap(it)
                }
        }
    }

    fun clear(target: ImageView) = jobs.remove(target)?.cancel()

    fun clearAll() {
        jobs.clear()
        scope.cancel()
    }

    private fun loadImage(url: String): Flow<Bitmap> = flow {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response: Response = okHttpClient
            .newCall(request)
            .await()

        val bufferedInputStream = BufferedInputStream(response.body()?.byteStream())
        emit(BitmapFactory.decodeStream(bufferedInputStream))
    }.flowOn(Dispatchers.IO)
}
