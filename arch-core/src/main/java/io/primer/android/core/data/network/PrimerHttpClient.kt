package io.primer.android.core.data.network

import io.primer.android.core.data.error.model.APIError
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.data.network.exception.InvalidUrlException
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.exception.JsonEncodingException
import io.primer.android.core.data.network.extensions.await
import io.primer.android.core.data.network.extensions.containsError
import io.primer.android.core.data.network.helpers.MessageLog
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.data.network.helpers.MessageTypeHelper
import io.primer.android.core.data.network.helpers.SeverityHelper
import io.primer.android.core.data.network.retry.NETWORK_EXCEPTION_ERROR_CODE
import io.primer.android.core.data.network.retry.RetryConfig
import io.primer.android.core.data.network.retry.SERVER_ERRORS
import io.primer.android.core.data.network.retry.isLastAttempt
import io.primer.android.core.data.network.retry.networkError
import io.primer.android.core.data.network.retry.retry
import io.primer.android.core.data.serialization.json.JSONArraySerializer
import io.primer.android.core.data.serialization.json.JSONDataUtils
import io.primer.android.core.data.serialization.json.JSONDataUtils.stringToJsonData
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializable
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.utils.EventFlowProvider
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

const val CONTENT_TYPE_APPLICATION_JSON = "application/json"

class PrimerHttpClient(
    val okHttpClient: OkHttpClient,
    val logProvider: EventFlowProvider<MessageLog>,
    val messagePropertiesEventProvider: EventFlowProvider<MessagePropertiesHelper>
) {

    inline fun <reified R : JSONDeserializable> get(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): Flow<PrimerResponse<R>> =
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

    suspend inline fun <reified R : JSONDeserializable> suspendGet(
        url: String,
        headers: Map<String, String> = hashMapOf()
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest<R>(
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
    ): PrimerResponse<R> {
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
    ): Flow<PrimerResponse<R>> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest<R>(
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
    ): Flow<PrimerResponse<R>> =
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

    suspend inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> suspendPost(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf()
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest<R>(
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
    ): PrimerResponse<R> {
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
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest<R>(
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
    ): PrimerResponse<R> {
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

    @Suppress("ComplexMethod")
    suspend inline fun <reified R : JSONDeserializable> executeRequest(
        request: Request,
        retryConfig: RetryConfig = RetryConfig(false)
    ): PrimerResponse<R> {
        var response: Response

        do {
            response = try {
                okHttpClient.newCall(request).await()
            } catch (exception: IOException) {
                if (retryConfig.enabled) {
                    networkError(request.url.toString())
                } else {
                    throw exception
                }
            }
        } while (retry(response, retryConfig, logProvider, messagePropertiesEventProvider))

        if (retryConfig.enabled) {
            if (response.containsError()) {
                val errorMessage = "Failed after ${retryConfig.retries} retries.\n" + when {
                    retryConfig.isLastAttempt() -> "Reached maximum retries (${retryConfig.maxRetries})."
                    response.code == NETWORK_EXCEPTION_ERROR_CODE -> "Network error."
                    response.code in SERVER_ERRORS -> "Server error: ${response.code}."
                    else -> ""
                }
                logRetryFailedAttempt(message = errorMessage)
                throw if (response.code == NETWORK_EXCEPTION_ERROR_CODE) {
                    IOException(errorMessage)
                } else {
                    HttpException(response.code, APIError.create(response))
                }
            } else {
                if (retryConfig.retries > 0) {
                    val message =
                        "Request succeeded after ${retryConfig.retries} retries. Status code: ${response.code}"
                    logRetrySuccessAttempt(message = message)
                }
            }
        } else if (response.containsError()) {
            throw HttpException(response.code, APIError.create(response))
        }

        try {
            val body = response.body
            val headers = response.headers.toMultimap()
            val bodyString = body?.string() ?: "{}"

            return when (val jsonData = stringToJsonData(bodyString)) {
                is JSONDataUtils.JSONData.JSONObjectData -> {
                    body?.close()
                    PrimerResponse(
                        body = JSONSerializationUtils.getJsonObjectDeserializer<R>().deserialize(jsonData.json),
                        headers = headers
                    )
                }

                is JSONDataUtils.JSONData.JSONArrayData -> {
                    body?.close()
                    PrimerResponse(
                        body = JSONSerializationUtils.getJsonArrayDeserializer<R>().deserialize(jsonData.json),
                        headers = headers
                    )
                }
            }
        } catch (expected: Exception) {
            throw JsonDecodingException(expected)
        }
    }

    suspend fun logRetrySuccessAttempt(message: String) {
        logProvider.getEventProvider().emit(MessageLog(message = message, severity = SeverityHelper.INFO))
        messagePropertiesEventProvider.getEventProvider().tryEmit(
            MessagePropertiesHelper(
                MessageTypeHelper.RETRY_SUCCESS,
                message,
                SeverityHelper.INFO
            )
        )
    }

    suspend fun logRetryFailedAttempt(message: String) {
        logProvider.getEventProvider().emit(MessageLog(message = message, severity = SeverityHelper.ERROR))
        messagePropertiesEventProvider.getEventProvider().tryEmit(
            MessagePropertiesHelper(
                MessageTypeHelper.RETRY_FAILED,
                message,
                SeverityHelper.ERROR
            )
        )
    }

    inline fun <reified T : JSONSerializable> getRequestBody(request: T): RequestBody {
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
