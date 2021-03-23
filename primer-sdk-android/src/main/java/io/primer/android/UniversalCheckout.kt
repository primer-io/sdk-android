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
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinApiExtension

internal enum class UXMode {
    CHECKOUT, ADD_PAYMENT_METHOD, STANDALONE_PAYMENT_METHOD,
}

object UniversalCheckout {

    private lateinit var checkout: InternalUniversalCheckout // FIXME can't hold ref to Context

    fun initialize(context: Context, fullToken: String, theme: UniversalCheckoutTheme? = null) {
        val clientToken = ClientToken.fromString(fullToken)
        val config = CheckoutConfig.create(clientToken = fullToken)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
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

        checkout = InternalUniversalCheckout(context, model, fullToken, Dispatchers.IO, theme)
    }

    fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
        checkout.paymentMethods = paymentMethods
    }

    fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
        checkout.getSavedPaymentMethods(callback)
    }

    fun showSavedPaymentMethods(listener: CheckoutEventListener) {
        checkout.showSavedPaymentMethods(listener)
    }

    @KoinApiExtension
    fun showCheckout(listener: CheckoutEventListener, amount: Int, currency: String) {
        checkout.showCheckout(listener, amount, currency)
    }

    @KoinApiExtension
    fun showStandalone(listener: CheckoutEventListener, paymentMethod: PaymentMethod) {
        checkout.showStandalone(listener, paymentMethod)
    }

    fun dismiss() {
        checkout.dismiss()
    }

    fun showProgressIndicator(visible: Boolean) {
        checkout.showProgressIndicator(visible)
    }

    fun showSuccess(autoDismissDelay: Int = 3000) {
        checkout.showSuccess(autoDismissDelay)
    }
}

internal class InternalUniversalCheckout constructor(
    private val context: Context, // FIXME we cannot hold a ref to a Context
    private val model: Model,
    private val fullToken: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val theme: UniversalCheckoutTheme? = null,
) {

    var paymentMethods: List<PaymentMethod> = emptyList()

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
    fun showSavedPaymentMethods(listener: CheckoutEventListener) {
        show(listener, UXMode.ADD_PAYMENT_METHOD)
    }

    @KoinApiExtension
    fun showCheckout(listener: CheckoutEventListener, amount: Int, currency: String) {
        show(listener, UXMode.CHECKOUT, amount = amount, currency = currency)
    }

    @KoinApiExtension
    fun showStandalone(listener: CheckoutEventListener, paymentMethod: PaymentMethod) {
        paymentMethods = listOf(paymentMethod)
        show(listener, UXMode.STANDALONE_PAYMENT_METHOD)
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
    private fun show(listener: CheckoutEventListener, uxMode: UXMode? = null, amount: Int? = null, currency: String? = null) {
        subscription?.unregister()

        this.listener = listener
        this.subscription = EventBus.subscribe(eventBusListener)

        WebviewInteropRegister.init(context.packageName)

        val config = CheckoutConfig.create(
            clientToken = fullToken,
            uxMode = uxMode ?: UXMode.CHECKOUT,
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
