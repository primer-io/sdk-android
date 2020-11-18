package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.logging.Logger

class PrimerCheckout(private val context: Context, private val clientToken: String) {
    private val log = Logger("primer")

    fun show() {
        val intent = Intent(context, CheckoutActivity::class.java)
        intent.putExtra("token", clientToken)
        context.startActivity(intent)
    }
}