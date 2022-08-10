package io.primer.android.model

import io.primer.android.http.exception.HttpException
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.tokenization.models.TokenizationRequest
import io.primer.android.data.error.model.APIError
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// TODO move this somewhere else
internal suspend inline fun Call.await(): Response =
    suspendCancellableCoroutine { continuation ->
        val callback = object : Callback, CompletionHandler {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun invoke(cause: Throwable?) {
                try {
                    cancel()
                } catch (_: Throwable) {
                    // do nothing
                }
            }
        }
        enqueue(callback)
        continuation.invokeOnCancellation(callback)
    }

// FIXME drop Model class (in favour of something like PrimerService or PrimerApi)
// FIXME extract parsing to collaborator
internal class Model constructor(
    private val okHttpClient: OkHttpClient,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val json: Json,
) {

    // FIXME avoid this null-assertion
    private val session: ConfigurationData
        get() = localConfigurationDataSource.getConfiguration()

    fun tokenize(
        tokenizationRequest: TokenizationRequest,
    ): Flow<PaymentMethodTokenInternal> =
        flow {

            val map =
                json.encodeToJsonElement(tokenizationRequest).jsonObject
                    .filterNot { it.key == "type" }
            val body =
                RequestBody.create(MediaType.get("application/json"), json.encodeToString(map))

            // FIXME extra endpoint construction to collaborator (non-static call)
            val url =
                APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)
            val request = Request.Builder()
                .url(url)
                .headers(
                    Headers.of(
                        mapOf(
                            SDK_API_VERSION_HEADER to ApiVersion.PAYMENT_INSTRUMENTS_VERSION.version
                        )
                    )
                )
                .post(body)
                .build()

            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                // TODO extract error parsing to collaborator & pass error through OperationResult
                throw HttpException(response.code(), error)
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body()?.string() ?: "{}")
                response.body()?.close()
                continuation.resume(json)
            }
            val token: PaymentMethodTokenInternal = json
                .decodeFromString(PaymentMethodTokenInternal.serializer(), jsonBody.toString())

            emit(token)
        }

    // TODO refactor to proper repositories
    suspend fun post(
        pathname: String,
        paymentMethodType: PaymentMethodType,
        requestBody: JSONObject? = null,
    ): OperationResult<JSONObject> {

        val url = APIEndpoint.get(session, APIEndpoint.Target.CORE, pathname)
        val body = toJsonRequestBody(requestBody)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                throw HttpException(response.code(), error)
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body()?.string() ?: "{}")
                response.body()?.close()
                continuation.resume(json)
            }

            OperationResult.Success(jsonBody)
        } catch (error: Throwable) {
            OperationResult.Error(
                when {
                    error is HttpException && error.isClientError() ->
                        SessionCreateException(
                            paymentMethodType,
                            error.error.diagnosticsId
                        )
                    else -> error
                }
            )
        }
    }

    private fun toJsonRequestBody(requestBody: JSONObject?): RequestBody {
        val mimeType = MediaType.get("application/json")
        val serialized = requestBody?.toString() ?: "{}"
        return RequestBody.create(mimeType, serialized)
    }
}
