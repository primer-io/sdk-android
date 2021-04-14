package io.primer.android.payment.google

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//val walletOptions = Wallet.WalletOptions.Builder()
//    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
//    .build()
//val paymentsClient: PaymentsClient = Wallet.getPaymentsClient(this, walletOptions)

// TODO rename to facade
class GooglePayBridge(
    private val paymentsClient: PaymentsClient,
) {

    // allowedCardNetworks = listOf("AMEX", "DISCOVER", "INTERAC", "JCB", "MASTERCARD", "VISA")
    // allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    fun buildIsReadyToGooglePayRequest(
        gatewayMerchantId: String,
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean,
    ): JSONObject {
        val gatewayParams = JSONObject(
            mapOf(
                "gateway" to "primer",
                "gatewayMerchantId" to gatewayMerchantId
            )
        )
        val gatewayTokenizationSpecification = JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", gatewayParams)
        }

        val baseCardPaymentMethods = JSONObject().apply {
            val parameters = JSONObject().apply {
                put("allowedAuthMethods", JSONArray(allowedCardAuthMethods))
                put("allowedCardNetworks", JSONArray(allowedCardNetworks))
                put("billingAddressRequired", billingAddressRequired)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            }

            put("type", "CARD")
            put("parameters", parameters)
            put("tokenizationSpecification", gatewayTokenizationSpecification)
        }

        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethods))
        }
    }

    suspend fun checkIfIsReadyToPay(request: JSONObject): Boolean =
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
                        continuation.resumeWithException(exception)
                    }
                }
        }
}
