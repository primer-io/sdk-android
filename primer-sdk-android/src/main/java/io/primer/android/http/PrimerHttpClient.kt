package io.primer.android.http

import io.primer.android.analytics.data.helper.MessagePropertiesEventProvider
import io.primer.android.analytics.data.models.MessageProperties
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.serialization.json.JSONArraySerializer
import io.primer.android.core.serialization.json.JSONDataUtils.JSONData.JSONArrayData
import io.primer.android.core.serialization.json.JSONDataUtils.JSONData.JSONObjectData
import io.primer.android.core.serialization.json.JSONDataUtils.stringToJsonData
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.error.model.APIError
import io.primer.android.data.extensions.await
import io.primer.android.http.exception.HttpException
import io.primer.android.http.exception.InvalidUrlException
import io.primer.android.http.exception.JsonDecodingException
import io.primer.android.http.exception.JsonEncodingException
import io.primer.android.http.retry.NETWORK_EXCEPTION_ERROR_CODE
import io.primer.android.http.retry.RetryConfig
import io.primer.android.http.retry.SERVER_ERRORS
import io.primer.android.http.retry.isLastAttempt
import io.primer.android.http.retry.networkError
import io.primer.android.http.retry.retry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

private const val CONTENT_TYPE_APPLICATION_JSON = "application/json"

internal class PrimerHttpClient(
    private val okHttpClient: OkHttpClient,
    private val logReporter: LogReporter,
    private val messagePropertiesEventProvider: MessagePropertiesEventProvider
) {

    inline fun <reified R : JSONDeserializable> get(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): Flow<R> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(headers.toHeaders())
                        .get()
                        .build()
                )
            )
        }

    inline fun <reified R : JSONDeserializable> retryGet(
        url: String,
        headers: Map<String, String> = hashMapOf(),
        retryConfig: RetryConfig
    ): Flow<R> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(headers.toHeaders())
                        .get()
                        .build(),
                    retryConfig
                )
            )
        }

    suspend inline fun <reified R : JSONDeserializable> suspendGet(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): R {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .get()
                .build()
        )
    }

    suspend inline fun <reified R : JSONDeserializable> retrySuspendGet(
        url: String,
        headers: Map<String, String> = hashMapOf(),
        retryConfig: RetryConfig
    ): R {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .get()
                .build(),
            retryConfig
        )
    }

    inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> post(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf()
    ): Flow<R> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(headers.toHeaders())
                        .post(getRequestBody(request))
                        .build()
                )
            )
        }

    inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> retryRost(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf(),
        retryConfig: RetryConfig
    ): Flow<R> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(headers.toHeaders())
                        .post(getRequestBody(request))
                        .build(),
                    retryConfig
                )
            )
        }

    suspend inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> postSuspend(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf()
    ): R {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .post(getRequestBody(request))
                .build()
        )
    }

    suspend inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> retryPostSuspend(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf(),
        retryConfig: RetryConfig
    ): R {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .post(getRequestBody(request))
                .build(),
            retryConfig
        )
    }

    suspend inline fun <reified R : JSONDeserializable> delete(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): R {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .delete()
                .build()
        )
    }

    suspend inline fun <reified R : JSONDeserializable> retryDelete(
        url: String,
        headers: Map<String, String> = hashMapOf(),
        retryConfig: RetryConfig
    ): R {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .delete()
                .build(),
            retryConfig
        )
    }

    private suspend inline fun <reified R : JSONDeserializable> executeRequest(
        request: Request,
        retryConfig: RetryConfig = RetryConfig(false)
    ): R {
        var response: Response

        do {
            response = try {
                okHttpClient.newCall(request).await()
            } catch (exception: IOException) {
                networkError(request.url.toString())
            }
        } while (retry(response, retryConfig, logReporter, messagePropertiesEventProvider))

        if (retryConfig.enabled) {
            if (response.isSuccessful && retryConfig.retries > 0) {
                val message = "Request succeeded after ${retryConfig.retries} retries. Status code: ${response.code}"
                logReporter.info(message)
                messagePropertiesEventProvider.getMessageEventProvider().tryEmit(
                    MessageProperties(
                        MessageType.RETRY_SUCCESS,
                        message,
                        Severity.INFO
                    )
                )
            } else {
                val errorMessage = "Failed after ${retryConfig.retries} retries.\n" + when {
                    retryConfig.isLastAttempt() -> "Reached maximum retries (${retryConfig.maxRetries})."
                    response.code == NETWORK_EXCEPTION_ERROR_CODE -> "Network error."
                    response.code in SERVER_ERRORS -> "Server error: ${response.code}."
                    else -> ""
                }
                logReporter.error(errorMessage)
                messagePropertiesEventProvider.getMessageEventProvider().tryEmit(
                    MessageProperties(
                        MessageType.RETRY_FAILED,
                        errorMessage,
                        Severity.ERROR
                    )
                )

                throw if (response.code == NETWORK_EXCEPTION_ERROR_CODE) {
                    IOException(errorMessage)
                } else {
                    HttpException(response.code, APIError.create(response))
                }
            }
        }

        try {
            val body = response.body
            val bodyString = body?.string() ?: "{}"

            return when (val jsonData = stringToJsonData(bodyString)) {
                is JSONObjectData -> {
                    body?.close()
                    JSONSerializationUtils.getJsonObjectDeserializer<R>().deserialize(jsonData.json)
                }

                is JSONArrayData -> {
                    body?.close()
                    JSONSerializationUtils.getJsonArrayDeserializer<R>().deserialize(jsonData.json)
                }
            }
        } catch (expected: Exception) {
            throw JsonDecodingException(expected)
        }
    }

    private inline fun <reified T : JSONSerializable> getRequestBody(request: T): RequestBody {
        return try {
            val serialized = when (val serializer = JSONSerializationUtils.getJsonSerializer<T>()) {
                is JSONObjectSerializer -> serializer.serialize(request).toString()
                is JSONArraySerializer -> serializer.serialize(request).toString()
            }
            serialized.toRequestBody(CONTENT_TYPE_APPLICATION_JSON.toMediaType())
        } catch (expected: Exception) {
            throw JsonEncodingException(expected)
        }
    }
}
