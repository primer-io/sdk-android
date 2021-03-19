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
import org.koin.core.component.KoinApiExtension
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

internal class Model constructor(
    private val api: IAPIClient,
    private val clientToken: ClientToken,
    private val config: CheckoutConfig,
    private val okHttpClient: OkHttpClient? = null,
) {

    private var clientSession: ClientSession? = null

    private val session: ClientSession
        get() = clientSession!!

    suspend fun getConfiguration(): OperationResult<ClientSession> {
        val request = Request.Builder()
            .url(clientToken.configurationUrl)
            .get()
            .build()

        return try {
            val response: Response = okHttpClient!! // FIXME remove !!
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                response.body?.close()
                continuation.resume(json)
            }
            val clientSession = json.decodeFromString<ClientSession>(jsonBody.toString()) // needs to be extracted
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

        try {
            val response: Response = okHttpClient!! // FIXME remove !!
                .newCall(request)
                .await()

            val body: ResponseBody? = response.body

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                body?.close()
                continuation.resume(json)
            }
            val array = jsonBody.getJSONArray("data")
            val list = json.decodeFromString<List<PaymentMethodTokenInternal>>(array.toString()) // needs to be extracted

            return OperationResult.Success(list)
        } catch (error: Throwable) {
            return OperationResult.Error(error)
        }
    }

    suspend fun _tokenize(tokenizable: PaymentMethodDescriptor): OperationResult<PaymentMethodTokenInternal> {
        val requestBody = JSONObject().apply {
            put("paymentInstrument", tokenizable.toPaymentInstrument())
            if (config.uxMode == UXMode.ADD_PAYMENT_METHOD) {
                put("tokenType", TokenType.MULTI_USE.name)
                put("paymentFlow", "VAULT")
            }
        }

        val url = APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response: Response = okHttpClient!! // FIXME remove !!
                .newCall(request)
                .await()

            if (!response.isSuccessful) {
                val error = APIError.create(response)
                EventBus.broadcast(CheckoutEvent.TokenizationError(error))
                return OperationResult.Error(Throwable())
            }

            val jsonBody: JSONObject = suspendCancellableCoroutine { continuation ->
                val json = JSONObject(response.body?.string() ?: "{}")
                response.body?.close()
                continuation.resume(json)
            }
            val token: PaymentMethodTokenInternal = json.decodeFromString(jsonBody.toString())
            EventBus.broadcast(
                CheckoutEvent.TokenizationSuccess(PaymentMethodTokenAdapter.internalToExternal(token))
            )
            if (token.tokenType == TokenType.MULTI_USE) {
                EventBus.broadcast(CheckoutEvent.TokenAddedToVault(PaymentMethodTokenAdapter.internalToExternal(token)))
            }

            OperationResult.Success(token)
        } catch (error: Throwable) {
            OperationResult.Error(error)
        }
    }

    @KoinApiExtension
    fun tokenize(tokenizable: PaymentMethodDescriptor): Observable {
        val json = JSONObject()
        json.put("paymentInstrument", tokenizable.toPaymentInstrument())
        if (config.uxMode == UXMode.ADD_PAYMENT_METHOD) {
            json.put("tokenType", TokenType.MULTI_USE.name)
            json.put("paymentFlow", "VAULT")
        }

        val url = APIEndpoint.get(session, APIEndpoint.Target.PCI, APIEndpoint.PAYMENT_INSTRUMENTS)

        return api.post(url, json).observe {
            when (it) {
                is Observable.ObservableSuccessEvent -> {
                    handleTokenizationResult(it)
                }
                is Observable.ObservableErrorEvent -> {
                    handleTokenizationResult(it)
                }
            }
        }
    }

    fun deleteToken(token: PaymentMethodTokenInternal): Observable {
        val url = APIEndpoint.get(
            session,
            APIEndpoint.Target.PCI,
            APIEndpoint.DELETE_TOKEN,
            params = mapOf("id" to token.token)
        )

        return api.delete(url).observe {
            if (it is Observable.ObservableSuccessEvent) {
                EventBus.broadcast(CheckoutEvent.TokenRemovedFromVault(PaymentMethodTokenAdapter.internalToExternal(token)))
            }
        }
    }

    fun post(pathname: String, body: JSONObject? = null): Observable {
        return api.post(APIEndpoint.get(session, APIEndpoint.Target.CORE, pathname), body)
    }

    private fun handleTokenizationResult(e: Observable.ObservableSuccessEvent) {
        val token: PaymentMethodTokenInternal = e.cast()

        EventBus.broadcast(CheckoutEvent.TokenizationSuccess(PaymentMethodTokenAdapter.internalToExternal(token)))

        if (token.tokenType == TokenType.MULTI_USE) {
            EventBus.broadcast(CheckoutEvent.TokenAddedToVault(PaymentMethodTokenAdapter.internalToExternal(token)))
        }
    }

    private fun handleTokenizationResult(e: Observable.ObservableErrorEvent) {
        EventBus.broadcast(CheckoutEvent.TokenizationError(e.error))
    }
}
