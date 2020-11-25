package com.example.myapplication

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.primer.android.UniversalCheckout


class MainActivity : AppCompatActivity() {
    private lateinit var checkout: UniversalCheckout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            loadCheckout()
        }
    }

    private fun loadCheckout() {
        val queue = Volley.newRequestQueue(this)

        queue.add(
            JsonObjectRequest(
                Request.Method.POST,
                "http://192.168.0.105/token",
                null,
                { response -> renderCheckout(response.getString("clientToken")) },
                { error -> onError(error) }
            )
        )
    }

    private fun renderCheckout(token: String) {
        Log.i("primer.ExampleApp", "Creating checkout with token: $token")
        checkout = UniversalCheckout(this, token, uxMode = UniversalCheckout.UXMode.CHECKOUT, amount = 3500, currency = "SEK")
        checkout.show()
    }

    private fun onError(error: VolleyError) {
        Log.e("primer.ExampleApp", "Volley Error when getting client token!")
        Log.e("primer.ExampleApp", error.toString())
    }
}