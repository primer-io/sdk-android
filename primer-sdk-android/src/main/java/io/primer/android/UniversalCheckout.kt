package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.logging.Logger
import io.primer.android.model.json
import io.primer.android.ui.CheckoutSheetActivity
import kotlinx.serialization.serializer

class UniversalCheckout private constructor(
    private val context: Context,
    authTokenProvider: IClientTokenProvider,
) {
    private val log = Logger("primer")
    private val token = DeferredToken(authTokenProvider)
    private var paymentMethods: List<PaymentMethod> = ArrayList()

    private var listener: IUniversalCheckoutListener? = null

    enum class UXMode {
        CHECKOUT, ADD_PAYMENT_METHOD,
    }

    fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
        this.paymentMethods = paymentMethods
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
            intent.putExtra("paymentMethods", json.encodeToString(serializer(), paymentMethods))

            context.startActivity(intent)
        }
    }

    fun destroy() {
        this.listener = null
    }

    companion object {
        private var instance: UniversalCheckout? = null

        /**
         * Initializes the Primer SDK with the Application context and a client token Provider
         */
        fun initialize(context: Context, authTokenProvider: IClientTokenProvider) {
            instance = instance ?: UniversalCheckout(context, authTokenProvider)
        }


        /**
         * Initializes the Primer SDK with the Application context. This method assumes that
         * the context also implements the IClientTokenProvider interface
         */
        fun initialize(context: Context) {
            initialize(context, context as IClientTokenProvider)
        }

        /**
         * Load the provided payment methods for use with the SDK
         */
        fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
            instance?.loadPaymentMethods(paymentMethods)
        }

        /**
         * Show the checkout sheet and attach a listener which will receive callback events
         */
        fun show(
            listener: IUniversalCheckoutListener,
            uxMode: UXMode? = null,
            amount: Int? = null,
            currency: String? = null,
        ) {
            instance?.show(listener, uxMode, amount, currency)
        }

        /**
         * Destroy the primer checkout and release any resources
         */
        fun destroy() {
            instance?.destroy()
            instance = null
        }
    }
}