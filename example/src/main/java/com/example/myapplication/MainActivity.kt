package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.primer.android.*
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.OrderItem
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.payment.card.Card
import org.json.JSONObject
import java.util.*

private const val CLIENT_TOKEN_URI: String = "https://api.sandbox.primer.io/auth/client-token"
//private const val CLIENT_TOKEN_URI: String =
//    "https://us-central1-primerdemo-8741b.cloudfunctions.net/clientToken"
private const val CUSTOMER_ID: String = "will-123"
private const val API_KEY: String = "b91c117b-3a89-4773-bfc7-58a24d8328a6"

class MainActivity : AppCompatActivity() {

    private val eventListener = object : CheckoutEventListener {
        override fun onCheckoutEvent(event: CheckoutEvent) {
            Log.i("ExampleApp", "Checkout event! ${event.type.name}")

            when (event) {
                is CheckoutEvent.TokenAddedToVault -> {
                    Log.i("ExampleApp", "Customer added a new payment method: ${event.data.token}")
                    Handler(Looper.getMainLooper()).post {
                        UniversalCheckout.showSuccess(autoDismissDelay = 2500)
                    }
                }
                is CheckoutEvent.ApiError -> {
                    Log.e("ExampleApp", "${event.data}")
                    UniversalCheckout.dismiss()
                }
                is CheckoutEvent.Exit -> {
                    if (event.data.reason == CheckoutExitReason.EXIT_SUCCESS) {
                        Log.i("ExampleApp", "Awesome")
                    }
                }
            }
        }
    }

    private val card = Card()

    private val paypal = PayPal()

    private val klarna = Klarna("brand new PS5")

    private val googlePay = GooglePay(
        merchantName = "Primer",
        totalPrice = "1000",
        countryCode = "UK",
        currencyCode = "GBP"
    )

    private val goCardless = GoCardless(
        companyName = "Luko AB",
        companyAddress = "123 French St, Francetown, France, FR3NCH",
        customerName = "Will Knowles",
        customerEmail = "will.jk01@gmail.com",
        customerAddressPostalCode = "864918",
        customerAddressLine1 = "123 Fake St",
        customerAddressCity = "Paris",
        customerAddressCountryCode = "FR"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val queue = Volley.newRequestQueue(this)

        queue.add(
            ClientTokenRequest(
                { response ->
                    val token = response.getString("clientToken")
                    initializeCheckout(token)
                },
                { error -> onError(error) }
            )
        )
    }

    private fun initializeCheckout(token: String) {
        UniversalCheckout.initialize(this, token, Locale("sv", "SE"))
        UniversalCheckout.loadPaymentMethods(listOf(klarna, googlePay))

        showCheckout()

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showCheckout()
        }
    }

    private fun showCheckout() {
        UniversalCheckout.showCheckout(
            context = this,
            listener = eventListener,
            amount = 99999,
            currency = "SEK",
            isStandalonePaymentMethod = false
        )
    }

    private fun onError(error: VolleyError) {
        Log.e("ExampleApp", "Volley Error when getting client token: $error")
    }
}

class ClientTokenRequest(
    onSuccess: Response.Listener<JSONObject>,
    onError: Response.ErrorListener,
) : JsonObjectRequest(
    Method.POST,
    CLIENT_TOKEN_URI,
    JSONObject().apply { put("customerId", CUSTOMER_ID) },
    onSuccess,
    onError,
) {

    override fun getHeaders(): MutableMap<String, String> =
        HashMap<String, String>().apply {
            if (API_KEY.isNotEmpty()) {
                put("X-Api-Key", API_KEY)
            }
        }

    override fun getBody(): ByteArray {
        val body = """
            {
                "customerId": "hCYs6vHqYCa7o3893C4s9Y464P13",
                "checkout": {
                    "paymentFlow": "PREFER_VAULT"
                }
            }
        """.trimIndent()
        return body.toByteArray()
    }
}
