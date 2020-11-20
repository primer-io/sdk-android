package io.primer.android

import android.content.Context
import android.content.Intent
import android.view.View
import io.primer.android.logging.Logger
import io.primer.android.ui.main.CheckoutView

class PrimerCheckout(private val context: Context, private val clientToken: String) {
    private val log = Logger("primer")

    fun show() {
        log("Mounting...")
        log("Creating Layout")

        val intent = Intent(context, CheckoutSheetActivity::class.java)

        intent.putExtra("token", clientToken)
        context.startActivity(intent)
    }
}