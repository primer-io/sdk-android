package io.primer.android.payment.google

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// TODO rename to facade
class GooglePayBridge(
    private val paymentsClient: PaymentsClient,
) {

    fun buildPaymentRequest(
        gatewayMerchantId: String,
    ) {
//        val gatewayParams = JSONObject(
//            mapOf(
//                "gateway" to "primer",
//                "gatewayMerchantId" to gatewayMerchantId
//            )
//        )
//        val gatewayTokenizationSpecification = JSONObject().apply {
//            put("type", "PAYMENT_GATEWAY")
//            put("parameters", gatewayParams)
//        }

//            put("tokenizationSpecification", gatewayTokenizationSpecification)
    }

    fun buildIsReadyToGooglePayRequest(
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean,
    ): JSONObject {
        val baseCardPaymentMethods = JSONObject().apply {
            val parameters = JSONObject().apply {
                put("allowedAuthMethods", JSONArray(allowedCardAuthMethods))
                put("allowedCardNetworks", JSONArray(allowedCardNetworks))
                put("billingAddressRequired", billingAddressRequired)
            }

            put("type", "CARD")
            put("parameters", parameters)
        }

        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethods))
        }
    }

    suspend fun checkIfIsReadyToPay(
        gatewayMerchantId: String,
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean,
    ): Boolean = checkIfIsReadyToPay(
        buildIsReadyToGooglePayRequest(
            allowedCardNetworks = allowedCardNetworks,
            allowedCardAuthMethods = allowedCardAuthMethods,
            billingAddressRequired = billingAddressRequired
        )
    )

    private suspend fun checkIfIsReadyToPay(request: JSONObject): Boolean =
        suspendCoroutine { continuation: Continuation<Boolean> ->
            val isReadyToPayRequest = IsReadyToPayRequest.fromJson(request.toString())
            paymentsClient
                .isReadyToPay(isReadyToPayRequest)
                .addOnCompleteListener { completedTask ->
                    try {
                        completedTask.getResult(ApiException::class.java)?.let { isAvailable ->
                            continuation.resume(isAvailable)
                        }
                    } catch (exception: ApiException) {
                        // continuation.resumeWithException(exception)
                        // TODO log error
                        continuation.resume(false)
                    }
                }
        }
}
