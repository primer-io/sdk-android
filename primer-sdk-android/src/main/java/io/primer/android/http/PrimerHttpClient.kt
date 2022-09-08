package io.primer.android.http

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.error.model.APIError
import io.primer.android.http.exception.HttpException
import io.primer.android.http.exception.JsonDecodingException
import io.primer.android.http.exception.JsonEncodingException
import io.primer.android.model.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import kotlin.coroutines.resume

private const val CONTENT_TYPE_APPLICATION_JSON = "application/json"

internal class PrimerHttpClient(
    private val okHttpClient: OkHttpClient,
) {

    inline fun <reified R : JSONDeserializable> get(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): Flow<R> =
        flow {
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(Headers.of(headers))
                        .get()
                        .build()
                )
            )
        }

    inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> post(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf(),
    ): Flow<R> =
        flow {
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(Headers.of(headers))
                        .post(getRequestBody(request))
                        .build()
                )
            )
        }

    inline fun <reified R : JSONDeserializable> delete(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): Flow<R> =
        flow {
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(Headers.of(headers))
                        .delete()
                        .build()
                )
            )
        }

    private suspend inline fun <reified R : JSONDeserializable> executeRequest(
        request: Request
    ): R {
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

        return try {
            JSONSerializationUtils.getDeserializer<R>().deserialize(jsonBody)
        } catch (expected: Exception) {
            throw JsonDecodingException(expected)
        }
    }

    private inline fun <reified T : JSONSerializable> getRequestBody(request: T): RequestBody {
        return try {
            RequestBody.create(
                MediaType.get(CONTENT_TYPE_APPLICATION_JSON),
                JSONSerializationUtils.getSerializer<T>().serialize(request).toString()
            )
        } catch (expected: Exception) {
            throw JsonEncodingException(expected)
        }
    }
}
