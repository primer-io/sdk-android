package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.APIClient
import io.primer.android.model.DeferredToken
import io.primer.android.model.Model
import io.primer.android.model.Observable
import io.primer.android.model.dto.*
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientToken
import io.primer.android.model.json
import kotlinx.serialization.serializer
import org.koin.core.component.KoinApiExtension
import java.util.*
import kotlin.collections.ArrayList

class UniversalCheckout private constructor(
    private val context: Context,
    authTokenProvider: ClientTokenProvider,
    private val theme: UniversalCheckoutTheme? = null,
) : EventBus.EventListener {

    private val log = Logger("primer")
    private val token = DeferredToken(authTokenProvider)
    private var paymentMethods: List<PaymentMethod> = ArrayList()

    private var listener: EventListener? = null
    private var subscription: EventBus.SubscriptionHandle? = null

    interface EventListener {

        fun onCheckoutEvent(e: CheckoutEvent)
    }

    internal enum class UXMode {
        CHECKOUT, VAULT,
    }

    private fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
        this.paymentMethods = paymentMethods
    }

    /**
     * TODO: refactor API client & data layer
     */
    private fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
        token.observe {
            val config = CheckoutConfig.create(clientToken = it)
            val token = ClientToken.fromString(it)
            val client = APIClient(token)
            val model = Model(client, token, config)
            model.getConfiguration().observe { remoteConfig ->
                when (remoteConfig) {
                    is Observable.ObservableSuccessEvent -> {
                        model.getVaultedPaymentMethods().observe { vault ->
                            when (vault) {
                                is Observable.ObservableSuccessEvent -> {
                                    val internal: List<PaymentMethodTokenInternal> =
                                        vault.cast(key = "data", defaultValue = Collections.emptyList())
                                    callback(internal.map { PaymentMethodTokenAdapter.internalToExternal(it) })
                                }
                                is Observable.ObservableErrorEvent -> {
                                    callback(listOf())
                                }
                            }
                        }
                    }
                    is Observable.ObservableErrorEvent -> {
                        callback(listOf())
                    }
                }
            }
        }
    }

    @KoinApiExtension
    private fun show(
        listener: EventListener,
        uxMode: UXMode? = null,
        amount: Int? = null,
        currency: String? = null,
        standalone: Boolean = false,
    ) {
        subscription?.unregister()

        this.listener = listener
        this.subscription = EventBus.subscribe(this)

        WebviewInteropRegister.init(context.packageName)

        token.observe {
            val config = CheckoutConfig.create(
                clientToken = it,
                uxMode = uxMode ?: UXMode.CHECKOUT,
                amount = amount,
                currency = currency,
                theme = theme,
                standalone = standalone,
            )

            val intent = Intent(context, CheckoutSheetActivity::class.java)

            intent.putExtra("config", json.encodeToString(serializer(), config))
            intent.putExtra("paymentMethods", json.encodeToString(serializer(), paymentMethods))

            context.startActivity(intent)
        }
    }

    private fun destroy() {
        this.subscription?.unregister()
        this.subscription = null
    }

    override fun onEvent(e: CheckoutEvent) {
        if (e.public) {
            listener?.onCheckoutEvent(e)
        }
    }

    companion object {

        private var instance: UniversalCheckout? = null

        /**
         * Initializes the Primer SDK with the Application context and a client token Provider
         */
        fun initialize(
            context: Context,
            authTokenProvider: ClientTokenProvider,
            theme: UniversalCheckoutTheme? = null,
        ) {
            destroy()
            instance = UniversalCheckout(context, authTokenProvider, theme = theme)
        }

        @KoinApiExtension
        fun showVault(listener: UniversalCheckout.EventListener, standalone: Boolean = false) {
            return show(listener, UXMode.VAULT, standalone = standalone)
        }

        @KoinApiExtension
        fun showCheckout(listener: EventListener, amount: Int, currency: String, standalone: Boolean = false) {
            return show(listener, UXMode.CHECKOUT, amount = amount, currency = currency, standalone = standalone)
        }

        fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
            instance?.getSavedPaymentMethods(callback)
        }

        /**
         * Initializes the Primer SDK with the Application context. This method assumes that
         * the context also implements the IClientTokenProvider interface
         */
        fun initialize(context: Context, theme: UniversalCheckoutTheme? = null) {
            initialize(context, context as ClientTokenProvider, theme)
        }

        /**
         * Load the provided payment methods for use with the SDK
         */
        fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
            instance?.loadPaymentMethods(paymentMethods)
        }

        /**
         * Dismiss the checkout
         */
        fun dismiss() {
            EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_CLIENT))
        }

        /**
         * Toggle the loading screen
         */
        fun showProgressIndicator(visible: Boolean) {
            EventBus.broadcast(CheckoutEvent.ToggleProgressIndicator(visible))
        }

        /**
         * Show a success screen then dismiss
         */
        fun showSuccess(autoDismissDelay: Int = 3000) {
            EventBus.broadcast(CheckoutEvent.ShowSuccess(autoDismissDelay))
        }

        /**
         * Destroy the primer checkout and release any resources
         */
        fun destroy() {
            instance?.destroy()
            instance = null
        }

        /**
         * Show the checkout sheet and attach a listener which will receive callback events
         */
        @KoinApiExtension
        private fun show(
            listener: EventListener,
            uxMode: UXMode,
            amount: Int? = null,
            currency: String? = null,
            standalone: Boolean = false,
        ) {
            instance?.show(listener, uxMode, amount, currency, standalone)
        }
    }
}
