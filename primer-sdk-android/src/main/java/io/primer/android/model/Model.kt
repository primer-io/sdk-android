package io.primer.android.model

import io.primer.android.UXMode
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.dto.*
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val config: CheckoutConfig,
    private val okHttpClient: OkHttpClient,
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
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                response.body?.close()
                continuation.resume(json)
            }
            val clientSession = json.decodeFromString<ClientSession>(jsonBody.toString()) // TODO move parsing somewhere else
            this.clientSession = clientSession

            OperationResult.Success(clientSession)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    suspend fun getVaultedPaymentMethods(clientSession: ClientSession): OperationResult<List<PaymentMethodTokenInternal>> {

        val baseUrl = "${clientSession.pciUrl}/payment-instruments"
        val request = Request.Builder()
            .url(baseUrl)
            .get()
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            val body: ResponseBody? = response.body

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                body?.close()
                continuation.resume(json)
            }
            val array = jsonBody.getJSONArray("data")
            val list = json.decodeFromString<List<PaymentMethodTokenInternal>>(array.toString()) // TODO move parsing somewhere else

            OperationResult.Success(list)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    suspend fun tokenize(tokenizable: PaymentMethodDescriptor): OperationResult<PaymentMethodTokenInternal> {
        val requestBody = JSONObject().apply {
            put("paymentInstrument", tokenizable.toPaymentInstrument())
            if (config.uxMode == UXMode.ADD_PAYMENT_METHOD) {
                put("tokenType", TokenType.MULTI_USE.name)
                put("paymentFlow", "VAULT")
            }
        }

        // FIXME extra endpoint construction to collaborator (non-static call)
        val url = APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                EventBus.broadcast(CheckoutEvent.TokenizationError(error)) // FIXME remove EventBus
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                response.body?.close()
                continuation.resume(json)
            }
            val token: PaymentMethodTokenInternal = json.decodeFromString(jsonBody.toString())
            EventBus.broadcast(
                CheckoutEvent.TokenizationSuccess(
                    PaymentMethodTokenAdapter.internalToExternal(token)
                ) // FIXME remove EventBus
            )
            if (token.tokenType == TokenType.MULTI_USE) {
                EventBus.broadcast(
                    CheckoutEvent.TokenAddedToVault(
                        PaymentMethodTokenAdapter.internalToExternal(token)
                    )
                ) // FIXME remove EventBus
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

    suspend fun post(pathname: String, requestBody: JSONObject? = null): OperationResult<JSONObject> {
        val url = APIEndpoint.get(session, APIEndpoint.Target.CORE, pathname)
        val stringifiedBody = requestBody?.toString() ?: "{}"
        val request = Request.Builder()
            .url(url)
            .post(stringifiedBody.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response: Response = okHttpClient
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                // TODO extract error parsing to collaborator & pass error through OperationResult
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                response.body?.close()
                continuation.resume(json)
            }

            OperationResult.Success(jsonBody)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }
}
