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

class MainActivity : AppCompatActivity(), IClientTokenProvider, IUniversalCheckoutListener {
    override fun createToken(callback: (String) -> Unit) {
        val queue = Volley.newRequestQueue(this)

        Log.i("primer.ExampleApp","Creating token")

        queue.add(
            JsonObjectRequest(
                Request.Method.POST,
                "http://100.65.23.18/token",
                null,
                { response -> callback(response.getString("clientToken")) },
                { error -> onError(error) }
            )
        )
    }

    override fun onTokenizationResult() {
        Log.i("primer.ExampleApp", "Tokenization finished!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        UniversalCheckout.initialize(this)
        UniversalCheckout.loadPaymentMethods(listOf(
            PaymentMethod.Card(),
            PaymentMethod.GooglePay(buttonColor = "white"),
            PaymentMethod.PayPal(buttonColor = "blue")
        ))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            renderCheckout()
        }
    }

    private fun renderCheckout() {
        Log.i("primer.ExampleApp", "Creating checkout")

        UniversalCheckout.show(this)
    }

    private fun onError(error: VolleyError) {
        Log.e("primer.ExampleApp", "Volley Error when getting client token!")
        Log.e("primer.ExampleApp", error.toString())
    }
}