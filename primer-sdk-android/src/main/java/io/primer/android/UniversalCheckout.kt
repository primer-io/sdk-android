package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.logging.Logger
import io.primer.android.ui.CheckoutSheetActivity
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class UniversalCheckout {
    private val log = Logger("primer")
    private val context: Context
    private val config: CheckoutConfig
    private val format = Json { ignoreUnknownKeys = true }

    enum class UXMode {
        CHECKOUT, ADD_PAYMENT_METHOD,
    }

    constructor(
        context: Context,
        clientToken: String,
        uxMode: UXMode = UXMode.CHECKOUT,
        amount: Int? = null,
        currency: String? = null,
    ) : this(
        context,
        CheckoutConfig.create(
            clientToken = clientToken,
            uxMode = uxMode,
            amount = amount,
            currency = currency
        )
    )

    private constructor(context: Context, config: CheckoutConfig) {
        this.context = context;
        this.config = config;
    }

    fun show() {
        log("Starting checkout activity")

        val intent = Intent(context, CheckoutSheetActivity::class.java)
        val serialized = format.encodeToString(serializer(), config)

        intent.putExtra("config", serialized)

        context.startActivity(intent)
    }
}