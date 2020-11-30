package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.logging.Logger
import io.primer.android.ui.CheckoutSheetActivity
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class UniversalCheckout private constructor(
    private val context: Context,
    authTokenProvider: IClientTokenProvider,
) {
    private val log = Logger("primer")
    private val json = Json { ignoreUnknownKeys = true }
    private val token = DeferredToken(authTokenProvider)

    private var listener: IUniversalCheckoutListener? = null

    enum class UXMode {
        CHECKOUT, ADD_PAYMENT_METHOD,
    }

    fun show(
        listener: IUniversalCheckoutListener,
        uxMode: UXMode? = null,
        amount: Int? = null,
        currency: String? = null,
    ) {
        log("Starting checkout activity")

        this.listener = listener

        token.observe {
            val config = CheckoutConfig.create(
                clientToken = it,
                uxMode = uxMode ?: UXMode.CHECKOUT,
                amount = amount,
                currency = currency
            )

            val intent = Intent(context, CheckoutSheetActivity::class.java)

            intent.putExtra("config", json.encodeToString(serializer(), config))

            context.startActivity(intent)
        }
    }

    fun destroy() {
        this.listener = null
    }

    companion object {
        private var instance: UniversalCheckout? = null

        fun initialize(context: Context, authTokenProvider: IClientTokenProvider) {
            instance = instance ?: UniversalCheckout(context, authTokenProvider)
        }

        fun initialize(context: Context) {
            initialize(context, context as IClientTokenProvider)
        }

        fun show(
            listener: IUniversalCheckoutListener,
            uxMode: UXMode? = null,
            amount: Int? = null,
            currency: String? = null,
        ) {
            instance?.show(listener, uxMode, amount, currency)
        }

        fun destroy() {
            instance?.destroy()
            instance = null
        }
    }
}