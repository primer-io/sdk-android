package io.primer.android.core.data.network

import io.primer.android.core.data.error.model.APIError
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.data.network.exception.InvalidUrlException
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.exception.JsonEncodingException
import io.primer.android.core.data.network.extensions.containsError
import io.primer.android.core.data.network.helpers.MessageLog
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.data.network.helpers.MessageTypeHelper
import io.primer.android.core.data.network.helpers.SeverityHelper
import io.primer.android.core.data.network.retry.HttpRetryLoop
import io.primer.android.core.data.network.retry.HttpTransportResult
import io.primer.android.core.data.network.retry.RetryAttempt
import io.primer.android.core.data.network.retry.RetryAttemptError
import io.primer.android.core.data.network.retry.RetryEventListener
import io.primer.android.core.data.network.retry.RetryPolicy
import io.primer.android.core.data.network.transport.HttpTransport
import io.primer.android.core.data.network.transport.RawHttpResponse
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.toJavaDuration

const val CONTENT_TYPE_APPLICATION_JSON = "application/json"

@Suppress("TooManyFunctions")
class PrimerHttpClient(
    val okHttpClient: OkHttpClient,
    val logProvider: EventFlowProvider<MessageLog>,
    val messagePropertiesEventProvider: EventFlowProvider<MessagePropertiesHelper>,
) {
    val transport: HttpTransport =
        HttpTransport(
            okHttpClient = okHttpClient,
            retryLoop = HttpRetryLoop(listener = AnalyticsRetryEventListener()),
        )

    fun withTimeout(duration: Duration?): PrimerHttpClient {
        if (duration == null) return this

        return customTimeoutInstances.getOrPut(duration) {
            PrimerHttpClient(
                okHttpClient =
                okHttpClient.newBuilder()
                    .readTimeout(duration.toJavaDuration())
                    .writeTimeout(duration.toJavaDuration())
                    .build(),
                logProvider = logProvider,
                messagePropertiesEventProvider = messagePropertiesEventProvider,
            )
        }
    }

    inline fun <reified R : JSONDeserializable> get(
        url: String,
        headers: Map<String, String> = hashMapOf(),
    ): Flow<PrimerResponse<R>> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest(
                    Request.Builder()
                        .url(url)
                        .headers(headers.toHeaders())
                        .get()
                        .build(),
                ),
            )
        }

    suspend inline fun <reified R : JSONDeserializable> suspendGet(
        url: String,
        headers: Map<String, String> = hashMapOf(),
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest<R>(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .get()
                .build(),
        )
    }

    suspend inline fun <reified R : JSONDeserializable> retrySuspendGet(
        url: String,
        headers: Map<String, String> = hashMapOf(),
        retryPolicy: RetryPolicy = RetryPolicy(),
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .get()
                .build(),
            retryPolicy,
        )
    }

    inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> post(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf(),
    ): Flow<PrimerResponse<R>> =
        flow {
            if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
            emit(
                executeRequest<R>(
                    Request.Builder()
                        .url(url)
                        .headers(headers.toHeaders())
                        .post(getRequestBody(request))
                        .build(),
                ),
            )
        }

    suspend inline fun <reified T : JSONSerializable, reified R : JSONDeserializable> suspendPost(
        url: String,
        request: T,
        headers: Map<String, String> = hashMapOf(),
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest<R>(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .post(getRequestBody(request))
                .build(),
        )
    }

    suspend inline fun <reified R : JSONDeserializable> delete(
        url: String,
        headers: Map<String, String> = hashMapOf(),
    ): PrimerResponse<R> {
        if (url.toHttpUrlOrNull() == null) throw InvalidUrlException(url = url)
        return executeRequest<R>(
            Request.Builder()
                .url(url)
                .headers(headers.toHeaders())
                .delete()
                .build(),
        )
    }

    @Suppress("ThrowsCount")
    suspend inline fun <reified R : JSONDeserializable> executeRequest(
        request: Request,
        retryPolicy: RetryPolicy? = null,
    ): PrimerResponse<R> =
        when (val outcome = transport.execute(request = request, retryPolicy = retryPolicy)) {
            is HttpTransportResult.Failed -> throw outcome.error
            is HttpTransportResult.Settled -> {
                val response = outcome.response
                if (!response.isSuccess || response.containsError()) {
                    throw HttpException(response.statusCode, APIError.create(response.bodyText))
                }
                deserializeResponse(response)
            }
        }

    inline fun <reified R : JSONDeserializable> deserializeResponse(response: RawHttpResponse): PrimerResponse<R> {
        // Only a NULL body coerces to an empty object; a blank body must keep failing
        // deserialization exactly like it did before the transport rework.
        val bodyString = response.bodyText ?: "{}"

        try {
            return when (val jsonData = stringToJsonData(bodyString)) {
                is JSONDataUtils.JSONData.JSONObjectData ->
                    PrimerResponse(
                        statusCode = response.statusCode,
                        body = JSONSerializationUtils.getJsonObjectDeserializer<R>().deserialize(jsonData.json),
                        headers = response.headers,
                    )

                is JSONDataUtils.JSONData.JSONArrayData ->
                    PrimerResponse(
                        statusCode = response.statusCode,
                        body = JSONSerializationUtils.getJsonArrayDeserializer<R>().deserialize(jsonData.json),
                        headers = response.headers,
                    )
            }
        } catch (expected: Exception) {
            throw JsonDecodingException(expected)
        }
    }

    inline fun <reified T : JSONSerializable> getRequestBody(request: T): RequestBody {
        return try {
            val serialized =
                when (val serializer = JSONSerializationUtils.getJsonSerializer<T>()) {
                    is JSONObjectSerializer -> serializer.serialize(request).toString()
                    is JSONArraySerializer -> serializer.serialize(request).toString()
                }
            serialized.toRequestBody(CONTENT_TYPE_APPLICATION_JSON.toMediaType())
        } catch (expected: Exception) {
            throw JsonEncodingException(expected)
        }
    }

    private inner class AnalyticsRetryEventListener : RetryEventListener {
        override suspend fun onRetryScheduled(
            attempt: RetryAttempt,
            maxAttempts: Int,
        ) {
            val reason =
                when (val error = attempt.error) {
                    is RetryAttemptError.Status -> "HTTP ${error.statusCode} error encountered"
                    is RetryAttemptError.Thrown ->
                        "network error encountered (${error.cause::class.java.simpleName})"
                }
            val message =
                "Retry attempt ${attempt.attempt} of $maxAttempts due to $reason. " +
                    "Waiting for ${attempt.delay}ms before next attempt."
            emitRetryEvent(MessageTypeHelper.RETRY, message, SeverityHelper.WARN)
        }

        override suspend fun onRecovered(
            retries: Int,
            statusCode: Int,
        ) {
            val message = "Request succeeded after $retries retries. Status code: $statusCode"
            emitRetryEvent(MessageTypeHelper.RETRY_SUCCESS, message, SeverityHelper.INFO)
        }

        override suspend fun onExhausted(history: List<RetryAttempt>) {
            val reason =
                when (val error = history.lastOrNull()?.error) {
                    is RetryAttemptError.Status -> "Server error: ${error.statusCode}."
                    is RetryAttemptError.Thrown -> "Network error."
                    null -> ""
                }
            val message = "Failed after ${history.size} retries. $reason".trim()
            emitRetryEvent(MessageTypeHelper.RETRY_FAILED, message, SeverityHelper.ERROR)
        }

        private suspend fun emitRetryEvent(
            type: MessageTypeHelper,
            message: String,
            severity: SeverityHelper,
        ) {
            logProvider.getEventProvider().emit(MessageLog(message = message, severity = severity))
            messagePropertiesEventProvider.getEventProvider().tryEmit(
                MessagePropertiesHelper(
                    type,
                    message,
                    severity,
                ),
            )
        }
    }

    companion object {
        private val customTimeoutInstances = ConcurrentHashMap<Duration, PrimerHttpClient>()

        fun clearCustomTimeoutInstances() = customTimeoutInstances.clear()
    }
}
