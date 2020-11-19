package io.primer.android

import android.content.Context
import android.view.View
import io.primer.android.logging.Logger
import io.primer.android.ui.main.CheckoutView

class PrimerCheckout(private val context: Context, private val clientToken: String) {
    private val log = Logger("primer")

    fun mount() : View {
        log("Mounting...")
        log("Creating Layout")

        val view = CheckoutView(context)

        return view
    }
}