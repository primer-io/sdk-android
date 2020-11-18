package com.example.myapplication

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.primer.android.PrimerCheckout


class MainActivity : AppCompatActivity() {
    private lateinit var checkout: PrimerCheckout

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
                "http://10.0.2.2/token",
                null,
                { response -> renderCheckout(response.getString("clientToken")) },
                { error -> onError(error) }
            )
        )
    }

    private fun renderCheckout(token: String) {
        Log.i("primer.ExampleApp", "Creating checkout with token: $token")
        checkout = PrimerCheckout(this, token)
        checkout.show()
    }

    private fun onError(error: VolleyError) {
        Log.e("primer.ExampleApp", "Volley Error when getting client token!")
        Log.e("primer.ExampleApp", error.toString())
    }
}