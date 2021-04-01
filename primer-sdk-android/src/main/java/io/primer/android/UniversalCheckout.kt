package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.dto.*
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.ClientToken
import io.primer.android.model.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.component.KoinApiExtension

internal enum class UXMode {
    CHECKOUT, VAULT,
}

object UniversalCheckout {

    private lateinit var checkout: InternalUniversalCheckout

    /**
     * Initializes the Primer SDK with the Application context and a client token Provider
     */
    fun initialize(fullToken: String, theme: UniversalCheckoutTheme? = null) {
        val clientToken = ClientToken.fromString(fullToken)
        val config = CheckoutConfig(clientToken = fullToken)

        // FIXME inject these dependencies
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Primer-SDK-Version", BuildConfig.SDK_VERSION_STRING)
                    .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
                    .addHeader("Primer-Client-Token", clientToken.accessToken)
                    .build()
                    .let { chain.proceed(it) }
            }
            .build()

        val model = Model(clientToken, config, okHttpClient)

        checkout = InternalUniversalCheckout(model, fullToken, Dispatchers.IO, theme)
    }

    /**
     * Load the provided payment methods for use with the SDK
     */
    fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
        checkout.paymentMethods = paymentMethods
    }

    fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
        checkout.getSavedPaymentMethods(callback)
    }

    fun showVault(context: Context, listener: CheckoutEventListener, isStandalonePayment: Boolean = false) {
        checkout.showVault(context, listener, isStandalonePayment)
    }

    @KoinApiExtension
    fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int,
        currency: String,
        isStandalonePayment: Boolean = false,
    ) {
        checkout.showCheckout(context, listener, amount, currency, isStandalonePayment)
    }

    /**
     * Dismiss the checkout
     */
    fun dismiss() {
        checkout.dismiss()
    }

    /**
     * Toggle the loading screen
     */
    fun showProgressIndicator(visible: Boolean) {
        checkout.showProgressIndicator(visible)
    }

    /**
     * Show a success screen then dismiss
     */
    fun showSuccess(autoDismissDelay: Int = 3000) {
        checkout.showSuccess(autoDismissDelay)
    }
}

internal class InternalUniversalCheckout constructor(
    private val model: Model,
    private val fullToken: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val theme: UniversalCheckoutTheme? = null,
) {

    internal var paymentMethods: List<PaymentMethod> = emptyList()

    private var listener: CheckoutEventListener? = null
    private var subscription: EventBus.SubscriptionHandle? = null

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            if (e.public) {
                listener?.onCheckoutEvent(e)
            }
        }
    }

    fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
        // FIXME this needs to be moved to a viewmodel
        CoroutineScope(ioDispatcher).launch {
            when (val configResult = model.getConfiguration()) {
                is OperationResult.Success -> {
                    val clientSession: ClientSession = configResult.data
                    when (val result = model.getVaultedPaymentMethods(clientSession)) {
                        is OperationResult.Success -> {
                            val paymentMethodTokens: List<PaymentMethodTokenInternal> = result.data
                            callback(paymentMethodTokens.map { PaymentMethodTokenAdapter.internalToExternal(it) })
                        }
                        is OperationResult.Error -> {
                            callback(listOf())
                            // TODO anything else?
                        }
                    }
                }
                is OperationResult.Error -> {
                    callback(listOf())
                    // TODO anything else?
                }
            }
        }
    }

    @KoinApiExtension
    fun showVault(context: Context, listener: CheckoutEventListener, isStandalonePayment: Boolean) {
        show(context, listener, UXMode.VAULT, isStandalonePayment)
    }

    @KoinApiExtension
    fun showCheckout(context: Context, listener: CheckoutEventListener, amount: Int, currency: String, isStandalonePayment: Boolean) {
        show(context, listener, UXMode.CHECKOUT, isStandalonePayment, amount = amount, currency = currency)
    }

    fun dismiss() {
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_CLIENT))
    }

    fun showProgressIndicator(visible: Boolean) {
        EventBus.broadcast(CheckoutEvent.ToggleProgressIndicator(visible))
    }

    fun showSuccess(autoDismissDelay: Int = 3000) {
        EventBus.broadcast(CheckoutEvent.ShowSuccess(autoDismissDelay))
    }

    @KoinApiExtension
    private fun show(
        context: Context,
        listener: CheckoutEventListener,
        uxMode: UXMode,
        isStandalonePayment: Boolean,
        amount: Int? = null,
        currency: String? = null,
    ) {
        subscription?.unregister()

        this.listener = listener
        this.subscription = EventBus.subscribe(eventBusListener)

        WebviewInteropRegister.init(context.packageName)

        val config = CheckoutConfig(
            clientToken = fullToken,
            uxMode = uxMode,
            isStandalonePayment = isStandalonePayment,
            amount = amount,
            currency = currency,
            theme = theme,
        )

        Intent(context, CheckoutSheetActivity::class.java)
            .apply {
                putExtra("config", json.encodeToString(serializer(), config))
                putExtra("paymentMethods", json.encodeToString(serializer(), paymentMethods))
            }
            .run { context.startActivity(this) }
    }
}

interface CheckoutEventListener {

    fun onCheckoutEvent(e: CheckoutEvent)
}
