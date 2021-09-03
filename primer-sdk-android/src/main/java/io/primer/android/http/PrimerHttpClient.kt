package io.primer.android.http

import io.primer.android.data.exception.HttpException
import io.primer.android.model.await
import io.primer.android.model.dto.APIError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import kotlin.coroutines.resume

internal class PrimerHttpClient(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {

    inline fun <reified R> get(
        url: String,
    ): Flow<R> =
        flow {
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .get()
                        .build()
                )
            )
        }

    inline fun <reified T, reified R> post(
        url: String,
        request: T,
    ): Flow<R> =
        flow {
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .post(
                            RequestBody.create(
                                MediaType.get("application/json"),
                                json.encodeToString(request)
                            )
                        )
                        .build()
                )
            )
        }

    private suspend inline fun <reified R> executeRequest(request: Request): R {
        val response: Response = okHttpClient
            .newCall(request)
            .await()

        if (!response.isSuccessful) {
            val error = APIError.create(response)
            throw HttpException(response.code(), error)
        }

        val body: ResponseBody? = response.body()

        val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
            val json = JSONObject(response.body()?.string() ?: "{}")
            body?.close()
            continuation.resume(json)
        }

        return json.decodeFromString(
            jsonBody.toString(),
        )
    }
}
