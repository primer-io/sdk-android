package io.primer.android.model

import io.primer.android.UXMode
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.ClientToken
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.model.dto.TokenType
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// TODO move this somewhere else
internal suspend inline fun Call.await(): Response =
    suspendCancellableCoroutine<Response> { continuation ->
        val callback = object : Callback, CompletionHandler {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled()) {
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
    private val clientToken: ClientToken,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {

    private var clientSession: ClientSession? = null

    // FIXME avoid this null-assertion
    private val session: ClientSession
        get() = clientSession!!

    suspend fun getConfiguration(): OperationResult<ClientSession> {

        val request = Request.Builder()
            .url(clientToken.configurationUrl)
            .get()
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                // TODO extract error parsing to collaborator & pass error through OperationResult
                EventBus.broadcast(CheckoutEvent.ApiError(error))
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body()?.string() ?: "{}")
                response.body()?.close()
                continuation.resume(json)
            }

            // TODO move parsing somewhere else
            val clientSession = json
                .decodeFromString(ClientSession.serializer(), jsonBody.toString())

            this.clientSession = clientSession

            OperationResult.Success(clientSession)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    suspend fun getVaultedPaymentMethods(
        clientSession: ClientSession,
    ): OperationResult<List<PaymentMethodTokenInternal>> {

        val baseUrl = "${clientSession.pciUrl}/payment-instruments"
        val request = Request.Builder()
            .url(baseUrl)
            .get()
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                // TODO extract error parsing to collaborator & pass error through OperationResult
                EventBus.broadcast(CheckoutEvent.ApiError(error))
                return OperationResult.Error(Throwable())
            }

            val body: ResponseBody? = response.body()

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body()?.string() ?: "{}")
                body?.close()
                continuation.resume(json)
            }
            val array = jsonBody.getJSONArray("data")

            // TODO move parsing somewhere else
            val list = json.decodeFromString<List<PaymentMethodTokenInternal>>(
                ListSerializer(PaymentMethodTokenInternal.serializer()),
                array.toString(),
            )

            OperationResult.Success(list)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    suspend fun tokenize(
        tokenizable: PaymentMethodDescriptor,
        uxMode: UXMode,
    ): OperationResult<PaymentMethodTokenInternal> {

        val requestBody = JSONObject().apply {
            put("paymentInstrument", tokenizable.toPaymentInstrument())
            if (uxMode.isVault) {
                put("tokenType", TokenType.MULTI_USE.name)
                put("paymentFlow", "VAULT")
            }
        }

        val body = toJsonRequestBody(requestBody)

        // FIXME extra endpoint construction to collaborator (non-static call)
        val url = APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)
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
                // TODO extract error parsing to collaborator & pass error through OperationResult
                EventBus.broadcast(CheckoutEvent.TokenizationError(error))
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body()?.string() ?: "{}")
                response.body()?.close()
                continuation.resume(json)
            }
            val token: PaymentMethodTokenInternal = json
                .decodeFromString(PaymentMethodTokenInternal.serializer(), jsonBody.toString())

            EventBus.broadcast(
                CheckoutEvent.TokenizationSuccess(
                    PaymentMethodTokenAdapter.internalToExternal(token)
                )
            )

            if (token.tokenType == TokenType.MULTI_USE) {
                EventBus.broadcast(
                    CheckoutEvent.TokenAddedToVault(
                        PaymentMethodTokenAdapter.internalToExternal(token)
                    )
                )
            }

            OperationResult.Success(token)
        } catch (error: Throwable) {

            OperationResult.Error(error)
        }
    }

    suspend fun deleteToken(token: PaymentMethodTokenInternal): OperationResult<Unit> {

        // FIXME extra endpoint construction to collaborator (non-static call)
        val url = APIEndpoint.get(
            session,
            APIEndpoint.Target.PCI,
            APIEndpoint.DELETE_TOKEN,
            params = mapOf("id" to token.token)
        )
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                // TODO extract error parsing to collaborator & pass error through OperationResult
                EventBus.broadcast(CheckoutEvent.ApiError(error))
                return OperationResult.Error(Throwable())
            }

            EventBus.broadcast(
                CheckoutEvent.TokenRemovedFromVault(
                    PaymentMethodTokenAdapter.internalToExternal(token)
                )
            ) // FIXME remove EventBus

            OperationResult.Success(Unit)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    suspend fun post(
        pathname: String,
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
                // TODO extract error parsing to collaborator & pass error through OperationResult
                EventBus.broadcast(CheckoutEvent.ApiError(error))
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body()?.string() ?: "{}")
                response.body()?.close()
                continuation.resume(json)
            }

            OperationResult.Success(jsonBody)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    private fun toJsonRequestBody(requestBody: JSONObject?): RequestBody {
        val mimeType = MediaType.get("application/json")
        val serialized = requestBody?.toString() ?: "{}"
        return RequestBody.create(mimeType, serialized)
    }
}
