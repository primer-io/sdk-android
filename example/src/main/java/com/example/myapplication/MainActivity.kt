package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.primer.android.*
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.dto.CheckoutExitReason
import org.json.JSONObject

const val CLIENT_TOKEN_URI: String = "https://api.sandbox.primer.io/auth/client-token"
const val CUSTOMER_ID: String = "will-123"
const val API_KEY: String = "b91c117b-3a89-4773-bfc7-58a24d8328a6"

class MainActivity : AppCompatActivity() {

    private val eventListener = object : CheckoutEventListener {
        override fun onCheckoutEvent(event: CheckoutEvent) {
            Log.i("primer.ExampleApp", "Checkout event! ${event.type.name}")

            when (event) {
                is CheckoutEvent.TokenAddedToVault -> {
                    Log.i("primer.ExampleApp", "Customer added a new payment method: ${event.data.token}")
                    Handler(Looper.getMainLooper()).postDelayed({
                        UniversalCheckout.showSuccess(autoDismissDelay = 2000)
                    }, 500)
                }
                is CheckoutEvent.Exit -> {
                    if (event.data.reason == CheckoutExitReason.EXIT_SUCCESS) {
                        Log.i("primer.ExampleApp", "Awesome")
                    }
                }
            }
        }
    }

    private val card = PaymentMethod.Card()

    private val paypal = PaymentMethod.PayPal()

    private val goCardless = PaymentMethod.GoCardless(
        companyName = "Luko AB",
        companyAddress = "123 French St, Francetown, France, FR3NCH",
        customerName = "Will Knowles",
        customerEmail = "will.jk01@gmail.com",
        customerAddressPostalCode = "864918",
        customerAddressLine1 = "123 Fake St",
        customerAddressCity = "Paris",
        customerAddressCountryCode = "FR"
    )

    private val paymentMethods = listOf(
        goCardless,
    )

    class ClientTokenRequest(onSuccess: Response.Listener<JSONObject>, onError: Response.ErrorListener) : JsonObjectRequest(
        Request.Method.POST,
        CLIENT_TOKEN_URI,
        JSONObject().apply { put("customerId", CUSTOMER_ID) },
        onSuccess,
        onError,
    ) {

        override fun getHeaders(): MutableMap<String, String> {
            return HashMap<String, String>().apply {
                if (API_KEY.isNotEmpty()) {
                    put("X-Api-Key", API_KEY)
                }
            }
        }
    }

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
        UniversalCheckout.initialize(token)
        UniversalCheckout.loadPaymentMethods(paymentMethods)
        showCheckout()

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            Log.i("primer.ExampleApp", "Creating checkout")
            showCheckout()
        }
    }

    private fun showCheckout() {
        UniversalCheckout.showVault(this, eventListener, isStandalonePayment = true)
    }

    private fun onError(error: VolleyError) {
        Log.e("primer.ExampleApp", "Volley Error when getting client token: $error")
    }
}
