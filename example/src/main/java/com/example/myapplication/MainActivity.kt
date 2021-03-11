package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.primer.android.*
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.dto.CheckoutExitReason
import org.json.JSONObject

class MainActivity : AppCompatActivity(), ClientTokenProvider, UniversalCheckout.EventListener {

    override fun createToken(callback: (String) -> Unit) {
        val queue = Volley.newRequestQueue(this)

        Log.i("primer.ExampleApp", "Creating token")

        val body = JSONObject()

        body.put("customerId", "will-123")

        queue.add(
            JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2/token",
                body,
                { response -> callback(response.getString("clientToken")) },
                { error -> onError(error) }
            )
        )
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        UniversalCheckout.initialize(this)

        UniversalCheckout.loadPaymentMethods(listOf(
            PaymentMethod.Card(),
            PaymentMethod.GoCardless(
                companyName = "Luko AB",
                companyAddress = "123 French St, Francetown, France, FR3NCH",
                customerName = "Will Knowles",
                customerEmail = "will.jk01@gmail.com",
                customerAddressPostalCode = "864918",
                customerAddressLine1 = "123 Fake St",
                customerAddressCity = "Paris",
                customerAddressCountryCode = "FR"
            ),
        ))

        UniversalCheckout.showSavedPaymentMethods(this)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            Log.i("primer.ExampleApp", "Creating checkout")
            UniversalCheckout.showSavedPaymentMethods(this)
        }
    }

    private fun onError(error: VolleyError) {
        Log.e("primer.ExampleApp", "Volley Error when getting client token: $error")
    }
}
