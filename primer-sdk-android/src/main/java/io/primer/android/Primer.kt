package io.primer.android

import android.content.Context
import android.content.Intent
import android.util.Log
import io.primer.android.data.action.models.actionSerializationModule
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.SdkSessionDataSource
import io.primer.android.analytics.data.models.AnalyticsSdkFunctionEventRequest
import io.primer.android.analytics.data.models.FunctionProperties
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.tokenization.models.tokenizationSerializationModule
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Serialization
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerPaymentMethod
import io.primer.android.model.CheckoutExitReason
import io.primer.android.data.token.model.ClientToken
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.EventDispatcher
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerIntent
import io.primer.android.data.settings.PrimerSettings
import kotlinx.serialization.encodeToString

class Primer private constructor() : PrimerInterface {

    internal var paymentMethods: MutableList<PaymentMethod> = mutableListOf()
    private var listener: PrimerCheckoutListener? = null
    private var config: PrimerConfig = PrimerConfig()
    private var subscription: EventBus.SubscriptionHandle? = null

    private val eventDispatcher = EventDispatcher()

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            addAnalyticsEvent(SdkFunctionParams("onEvent", mapOf("event" to e.type.name)))
            when (e) {
                is CheckoutEvent.TokenizationSuccess -> {
                    listener?.onTokenizeSuccess(e.data, e.resumeHandler)
                }
                is CheckoutEvent.ResumeSuccess -> {
                    listener?.onResumeSuccess(e.resumeToken, e.resumeHandler)
                }
                is CheckoutEvent.PaymentCreateStarted -> {
                    listener?.onBeforePaymentCreated(e.data, e.createPaymentHandler)
                }
                is CheckoutEvent.PaymentSuccess -> {
                    listener?.onCheckoutCompleted(e.data)
                }
                is CheckoutEvent.ClientSessionUpdateSuccess -> {
                    listener?.onClientSessionUpdated(e.data)
                }
                is CheckoutEvent.ClientSessionUpdateStarted -> {
                    listener?.onBeforeClientSessionUpdated()
                }
                is CheckoutEvent.Exit -> {
                    listener?.onDismissed()
                }
                is CheckoutEvent.CheckoutPaymentError -> {
                    listener?.onFailed(e.error, e.data, e.errorHandler)
                }
                is CheckoutEvent.CheckoutError -> {
                    listener?.onFailed(e.error, e.errorHandler)
                }
                else -> Unit
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun configure(
        settings: PrimerSettings?,
        listener: PrimerCheckoutListener?,
    ) {
        listener?.let { l -> setListener(l) }
        settings?.let {
            this.config = PrimerConfig(it)
        }
        addAnalyticsEvent(
            SdkFunctionParams(
                "configure",
                mapOf("settings" to Serialization.json.encodeToString(this.config))
            )
        )
    }

    override fun cleanup() {
        addAnalyticsEvent(SdkFunctionParams("cleanup"))
        listener = null
        subscription?.unregister(true)
        subscription = null
    }

    override fun showUniversalCheckout(context: Context, clientToken: String) {
        addAnalyticsEvent(SdkFunctionParams("showUniversalCheckout"))
        config.intent = PrimerIntent(PaymentMethodIntent.CHECKOUT, PrimerPaymentMethod.ANY)
        show(context, clientToken)
    }

    override fun showVaultManager(context: Context, clientToken: String) {
        addAnalyticsEvent(SdkFunctionParams("showVaultManager"))
        config.intent = PrimerIntent(PaymentMethodIntent.VAULT, PrimerPaymentMethod.ANY)
        show(context, clientToken)
    }

    override fun showPaymentMethod(
        context: Context,
        clientToken: String,
        paymentMethod: PrimerPaymentMethod,
        intent: PaymentMethodIntent,
    ) {
        addAnalyticsEvent(
            SdkFunctionParams(
                "showPaymentMethod",
                mapOf("paymentMethod" to paymentMethod.name, "intent" to intent.name)
            )
        )

        val flow = if (intent == PaymentMethodIntent.VAULT) PaymentMethodIntent.VAULT
        else PaymentMethodIntent.CHECKOUT
        config.intent = PrimerIntent(flow, paymentMethod)
        show(context, clientToken)
    }

    /**
     * Private method to set and subscribe using passed in listener. Clears previous subscriptions.
     */
    private fun setListener(listener: PrimerCheckoutListener) {
        subscription?.unregister(true)
        this.listener = null
        this.listener = listener
        subscription = EventBus.subscribe(eventBusListener)
    }

    /**
     * Private method to instantiate [CheckoutSheetActivity] and initialise the SDK.
     * Also configures any redirect schemes.
     */
    private fun show(context: Context, clientToken: String) {
        try {
            setupAndVerifyClientToken(clientToken)

            val scheme =
                config.settings.paymentMethodOptions.redirectScheme
                    ?: context.packageName.let { "$it.primer" }
            WebviewInteropRegister.init(scheme)

            // TODO: refactor the way we pass in the config.
            Serialization.addModule(tokenizationSerializationModule)
            Serialization.addModule(actionSerializationModule)

            val encodedConfig = Serialization.json.encodeToString(PrimerConfig.serializer(), config)
            Intent(context, CheckoutSheetActivity::class.java)
                .apply {
                    putExtra("config", encodedConfig)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
        } catch (e: Exception) {
            Log.e("Primer", e.message.toString())
            emitError(DefaultErrorMapper().getPrimerError(e))
        }
    }

    override fun dismiss(clearListeners: Boolean) {
        addAnalyticsEvent(SdkFunctionParams("dismiss"))
        if (clearListeners) listener = null
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_CLIENT))
    }

    internal fun addAnalyticsEvent(params: SdkFunctionParams) {
        LocalAnalyticsDataSource.instance.addEvent(
            AnalyticsSdkFunctionEventRequest(
                properties = FunctionProperties(params.name, params.params),
                sdkSessionId = SdkSessionDataSource.getSessionId()
            )
        )
    }

    private fun setupAndVerifyClientToken(clientToken: String) {
        ClientToken.fromString(clientToken)
        this.config.clientTokenBase64 = clientToken
    }

    private fun emitError(error: PrimerError) {
        when (config.settings.paymentHandling) {
            PrimerPaymentHandling.AUTO -> eventDispatcher.dispatchEvent(
                CheckoutEvent.CheckoutPaymentError(
                    error
                )
            )
            PrimerPaymentHandling.MANUAL -> eventDispatcher.dispatchEvent(
                CheckoutEvent.CheckoutError(
                    error
                )
            )
        }
    }

    companion object {

        /**
         * Singleton instance of [Primer]. Use this to call SDK methods.
         */
        @JvmStatic
        val instance: PrimerInterface by lazy { Primer() }
    }
}
