package io.primer.executionengine.data.executors.http

import io.primer.android.core.data.network.CONTENT_TYPE_APPLICATION_JSON
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.retry.HttpTransportResult
import io.primer.android.core.data.network.retry.RetryBackoff
import io.primer.android.core.data.network.retry.RetryPolicy
import io.primer.android.core.data.network.transport.RawHttpMethod
import io.primer.android.core.data.network.transport.RawHttpRequest
import io.primer.android.core.data.network.transport.RawHttpResponse
import io.primer.android.core.data.serialization.json.JSONDataUtils
import io.primer.android.core.data.serialization.json.extensions.toList
import io.primer.android.core.data.serialization.json.extensions.toMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.executionengine.data.models.http.HttpMethod
import io.primer.executionengine.data.models.http.HttpRequestParams
import io.primer.executionengine.data.models.http.HttpResponseOutput
import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.coroutines.cancellation.CancellationException

internal class HttpRequestStepExecutor(
    private val httpClient: PrimerHttpClient,
    private val requestIdProvider: () -> String,
) : StepExecutor {

    private val inFlightJobs = CopyOnWriteArraySet<Job>()

    override suspend fun execute(actionId: String, step: String): Result<StepResult> = runSuspendCatching {
        val params = HttpRequestParams.deserializer.deserialize(JSONObject(step))
        val method = validateMethod(params)
        val request = RawHttpRequest(
            method = RawHttpMethod.valueOf(method.name),
            url = params.url,
            headers = buildHeaders(params),
            body = serializeBody(params.body),
            timeoutMs = params.timeoutMs ?: DEFAULT_TIMEOUT_MS,
            requestId = requestIdProvider(),
        )
        when (val exchangeResult = executeAbortable(request = request, retryPolicy = params.toRetryPolicy())) {
            is HttpTransportResult.Failed -> throw exchangeResult.error
            is HttpTransportResult.Settled -> exchangeResult.response.toStepResult(actionId)
        }
    }

    override fun onFinish() {
        inFlightJobs.forEach { it.cancel() }
    }

    private fun validateMethod(params: HttpRequestParams): HttpMethod {
        val method = HttpMethod.safeValueOf(params.method) ?: throw IllegalArgumentException(
            "Unsupported HTTP method '${params.method}'. " +
                "Allowed methods: ${HttpMethod.entries.joinToString { it.name }}.",
        )
        require(!(method == HttpMethod.GET && params.body != null)) {
            "GET requests must not include a body."
        }
        return method
    }

    private fun buildHeaders(params: HttpRequestParams): Map<String, String> = buildMap {
        if (params.body != null) {
            put(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
        }
        params.idempotencyKey?.let { put(HEADER_IDEMPOTENCY_KEY, it) }
    }

    private fun HttpRequestParams.toRetryPolicy(): RetryPolicy? =
        retry?.let { retry ->
            RetryPolicy(
                maxAttempts = (retry.maxAttempts ?: DEFAULT_MAX_ATTEMPTS).coerceAtLeast(DEFAULT_MAX_ATTEMPTS),
                backoff = retry.backoff?.takeUnless { it == RetryBackoff.UNKNOWN } ?: RetryBackoff.EXPONENTIAL,
                baseDelayMs = retry.baseDelayMs ?: DEFAULT_BASE_DELAY_MS,
                retryOn = retry.retryOn ?: emptyList(),
            )
        }

    private suspend fun executeAbortable(
        request: RawHttpRequest,
        retryPolicy: RetryPolicy?,
    ): HttpTransportResult =
        try {
            coroutineScope {
                val job = coroutineContext.job
                inFlightJobs += job
                try {
                    httpClient.transport.execute(request = request, retryPolicy = retryPolicy)
                } finally {
                    inFlightJobs -= job
                }
            }
        } catch (expected: CancellationException) {
            // If the parent is cancelled, this is flow teardown: rethrow. Otherwise the
            // cancellation came from onFinish, so surface it as a step error.
            currentCoroutineContext().ensureActive()
            throw HttpStepAbortedException(cause = expected)
        }

    private fun RawHttpResponse.toStepResult(actionId: String): StepResult =
        StepResult(
            outcome = if (isSuccess) Outcome.SUCCESS else Outcome.ERROR,
            actionId = actionId,
            data = HttpResponseOutput(
                status = statusCode,
                success = isSuccess,
                headers = headers.toSingleValued(),
                body = parseBody(bodyText),
            ).toDataMap(),
        )

    private companion object {
        const val HEADER_CONTENT_TYPE = "Content-Type"
        const val HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key"
        const val DEFAULT_MAX_ATTEMPTS = 1
        const val DEFAULT_BASE_DELAY_MS = 250L
        const val DEFAULT_TIMEOUT_MS = 30_000L
    }
}

private fun serializeBody(body: Any?): String? =
    when (body) {
        null -> null
        is JSONObject, is JSONArray -> body.toString()
        is String -> JSONObject.quote(body)
        else -> body.toString()
    }

private fun Map<String, List<String>>.toSingleValued(): Map<String, String> =
    mapNotNull { (key, values) -> values.lastOrNull()?.let { value -> key to value } }.toMap()

private fun parseBody(bodyText: String?): Any? {
    if (bodyText.isNullOrBlank()) return null
    return runCatching {
        when (val jsonData = JSONDataUtils.stringToJsonData(bodyText)) {
            is JSONDataUtils.JSONData.JSONObjectData -> jsonData.json.toMap()
            is JSONDataUtils.JSONData.JSONArrayData -> jsonData.json.toList()
        }
    }.getOrDefault(bodyText)
}
