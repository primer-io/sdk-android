package io.primer.android.payment.google

import android.app.Activity
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

private object Constants {

    val SUPPORTED_NETWORKS = listOf(
//        "AMEX",
//        "DISCOVER",
//        "JCB",
        "MASTERCARD",
        "VISA"
    )

    val SUPPORTED_METHODS = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"
    )

    const val COUNTRY_CODE = "UK"
    const val CURRENCY_CODE = "GBP"

    val SHIPPING_SUPPORTED_COUNTRIES = listOf("US", "GB")
}

object PaymentsUtil {

    val MICROS: BigDecimal = BigDecimal(1000000.0)

    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    private fun gatewayTokenizationSpecification(id: String): JSONObject {
        val map = mapOf(
            "gateway" to "primer",
            "gatewayMerchantId" to id
        )
        val params = JSONObject(map)
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", params)
        }
    }

    private val allowedCardNetworks = JSONArray(Constants.SUPPORTED_NETWORKS)

    private val allowedCardAuthMethods = JSONArray(Constants.SUPPORTED_METHODS)

    // Optionally, you can add billing address/phone number associated with a CARD payment method.
    private fun baseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {

            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", true)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }

    private fun cardPaymentMethod(gatewayMerchantId: String): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod()
        cardPaymentMethod.put(
            "tokenizationSpecification",
            gatewayTokenizationSpecification(gatewayMerchantId)
        )

        return cardPaymentMethod
    }

    fun isReadyToPayRequest(): JSONObject? {
        return try {
            val isReadyToPayRequest = JSONObject(baseRequest.toString())
            isReadyToPayRequest.put(
                "allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod())
            )

            isReadyToPayRequest

        } catch (e: JSONException) {
            null
        }
    }

    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject().put("merchantName", "Example Merchant")

    private fun getTransactionInfo(price: String): JSONObject {
        return JSONObject().apply {
            put("totalPrice", price)
            put("totalPriceStatus", "FINAL")
            put("countryCode", Constants.COUNTRY_CODE)
            put("currencyCode", Constants.CURRENCY_CODE)
        }
    }

    fun getPaymentDataRequest(price: String, gatewayMerchantId: String): JSONObject? {
        try {
            return JSONObject(baseRequest.toString()).apply {
                put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod(gatewayMerchantId)))
                put("transactionInfo", getTransactionInfo(price))
                put("merchantInfo", merchantInfo)

                // An optional shipping address requirement is a top-level property of the
                // PaymentDataRequest JSON object.
                val shippingAddressParameters = JSONObject().apply {
                    put("phoneNumberRequired", false)
                    put("allowedCountryCodes", JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES))
                }
                put("shippingAddressRequired", true)
                put("shippingAddressParameters", shippingAddressParameters)
            }
        } catch (e: JSONException) {
            return null
        }

    }
}

fun Long.microsToString() = BigDecimal(this)
    .divide(PaymentsUtil.MICROS)
    .setScale(2, RoundingMode.HALF_EVEN)
    .toString()
