package io.primer.android.payment.google

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GooglePayFacade constructor(
    private val paymentsClient: PaymentsClient,
) {

    companion object {

        const val GOOGLE_PAY_REQUEST_CODE: Int = 1100
    }

    private val baseRequest: JSONObject = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    suspend fun checkIfIsReadyToPay(
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

    private fun buildIsReadyToGooglePayRequest(
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean,
    ): JSONObject {
        val baseCardPaymentMethods = baseCardPaymentMethod(
            allowedCardNetworks,
            allowedCardAuthMethods,
            billingAddressRequired
        )

        return baseRequest.apply {
            put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethods))
        }
    }

    private fun baseCardPaymentMethod(
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean,
    ): JSONObject {
        return JSONObject().apply {

            val parameters = JSONObject().apply {
                put("allowedAuthMethods", JSONArray(allowedCardAuthMethods))
                put("allowedCardNetworks", JSONArray(allowedCardNetworks))
                put("billingAddressRequired", billingAddressRequired)
                if (billingAddressRequired) {
                    put(
                        "billingAddressParameters",
                        JSONObject().apply {
                            put("format", "FULL")
                        }
                    )
                }
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }

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
                        // continuation.resumeWithException(exception) // TODO log error
                        continuation.resume(false)
                    }
                }
        }

    fun pay(
        activity: Activity,
        gatewayMerchantId: String,
        merchantName: String? = null,
        totalPrice: String,
        countryCode: String,
        currencyCode: String,
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean,
    ) {
        val request = buildPaymentRequest(
            gatewayMerchantId = gatewayMerchantId,
            merchantName = merchantName,
            totalPrice = totalPrice,
            countryCode = countryCode,
            currencyCode = currencyCode,
            allowedCardNetworks = allowedCardNetworks,
            allowedCardAuthMethods = allowedCardAuthMethods,
            billingAddressRequired = billingAddressRequired
        )
        pay(activity, request)
    }

    private fun buildPaymentRequest(
        gatewayMerchantId: String,
        merchantName: String? = null,
        totalPrice: String,
        countryCode: String,
        currencyCode: String,
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

        val cardPaymentMethod = baseCardPaymentMethod(
            allowedCardNetworks,
            allowedCardAuthMethods,
            billingAddressRequired
        ).apply {
            put("tokenizationSpecification", gatewayTokenizationSpecification)
        }

        val transactionInfo = JSONObject().apply {
            put("totalPrice", totalPrice)
            put("totalPriceStatus", "FINAL")
            put("countryCode", countryCode)
            put("currencyCode", currencyCode)
        }

        return JSONObject(baseRequest.toString()).apply {
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
            put("transactionInfo", transactionInfo)
            put("shippingAddressRequired", false)

            merchantName?.let {
                put("merchantInfo", JSONObject().put("merchantName", merchantName))
            }
        }
    }

    private fun pay(activity: Activity, request: JSONObject) {
        EventBus.broadcast(CheckoutEvent.PaymentMethodPresented)
        val paymentDataRequest = PaymentDataRequest.fromJson(request.toString())
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest),
            activity,
            GOOGLE_PAY_REQUEST_CODE
        )
    }

    enum class Environment {
        TEST,
        PRODUCTION
    }
}

class GooglePayFacadeFactory {

    fun create(
        applicationContext: Context,
        environment: GooglePayFacade.Environment,
    ): GooglePayFacade {
        val walletEnvironment =
            if (environment == GooglePayFacade.Environment.TEST) WalletConstants.ENVIRONMENT_TEST
            else WalletConstants.ENVIRONMENT_PRODUCTION
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(walletEnvironment)
            .build()
        val paymentsClient: PaymentsClient =
            Wallet.getPaymentsClient(applicationContext, walletOptions)

        return GooglePayFacade(paymentsClient)
    }
}
