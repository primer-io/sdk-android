package io.primer.executionengine.data.executors.http

import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.toMap
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.executionengine.data.models.http.HttpExecutionResult
import io.primer.executionengine.data.models.http.HttpMethod
import io.primer.executionengine.data.models.http.HttpResponse
import io.primer.executionengine.data.models.http.HttpStep
import io.primer.executionengine.domain.executor.StepExecutor
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

internal class HttpStepExecutor(
    private val httpClient: PrimerHttpClient,
) : StepExecutor {

    override suspend fun execute(actionId: String, step: String): Result<StepResult> = runSuspendCatching {
        val httpStep = HttpStep.deserializer.deserialize(JSONObject(step))

        val client = httpStep.timeout?.let {
            httpClient.withTimeout(it.milliseconds)
        } ?: httpClient

        val urlBuilder = httpStep.url.toHttpUrlOrNull()?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL: ${httpStep.url}")

        httpStep.query?.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        val headers = httpStep.headers?.toHeaders() ?: okhttp3.Headers.Builder().build()

        val body = httpStep.body?.let {
            JSONObject(it).toString()
                .toRequestBody("application/json".toMediaType())
        }

        val request = Request.Builder()
            .url(urlBuilder.build())
            .headers(headers)
            .method(httpStep.method.name, body.takeIf { httpStep.method != HttpMethod.GET })
            .build()

        val primerResponse = client.executeRequest<HttpResponse>(request)

        val result = HttpExecutionResult(
            statusCode = primerResponse.statusCode,
            response = JSONObject(primerResponse.body.data).toString(),
            headers = primerResponse.headers,
        )

        StepResult(
            outcome = Outcome.SUCCESS,
            actionId = actionId,
            data =
            JSONSerializationUtils.getJsonObjectSerializer<HttpExecutionResult>()
                .serialize(result).toMap(),
        )
    }
}
