package com.example.myapplication

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.primer.android.*
import io.primer.android.events.CheckoutEvent
import org.json.JSONObject

class MainActivity : AppCompatActivity(), ClientTokenProvider, UniversalCheckout.EventListener {
    override fun createToken(callback: (String) -> Unit) {
        val queue = Volley.newRequestQueue(this)

        Log.i("primer.ExampleApp","Creating token")

        val body = JSONObject()

        body.put("customerId", "will-123")

        queue.add(
            JsonObjectRequest(
                Request.Method.POST,
                "http://192.168.0.107/token",
                body,
                { response -> callback(response.getString("clientToken")) },
                { error -> onError(error) }
            )
        )
    }

    override fun onCheckoutEvent(e: CheckoutEvent) {
        Log.i("primer.ExampleApp", "Checkout event! ${e.type.name}")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        UniversalCheckout.initialize(this)

        UniversalCheckout.loadPaymentMethods(listOf(
            PaymentMethod.Card(),
            PaymentMethod.PayPal()
        ))

        showCheckout()

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showCheckout()
        }
    }

    private fun showCheckout() {
        Log.i("primer.ExampleApp", "Creating checkout")
        UniversalCheckout.showSavedPaymentMethods(this)
    }

    private fun onError(error: VolleyError) {
        Log.e("primer.ExampleApp", "Volley Error when getting client token!")
        Log.e("primer.ExampleApp", error.toString())
    }
}