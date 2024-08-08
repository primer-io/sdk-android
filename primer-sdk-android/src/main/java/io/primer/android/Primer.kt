package io.primer.android

import android.content.Context
import android.content.Intent
import android.util.Log
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.SdkSessionDataSource
import io.primer.android.analytics.data.models.AnalyticsSdkFunctionEventRequest
import io.primer.android.analytics.data.models.FunctionProperties
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.RawDataDelegate
import io.primer.android.data.configuration.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerIntent
import io.primer.android.data.token.model.ClientToken
import io.primer.android.di.DISdkComponent
import io.primer.android.di.DISdkContext
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.events.EventDispatcher
import io.primer.android.model.CheckoutExitReason

class Primer private constructor() : PrimerInterface, DISdkComponent {

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
                is CheckoutEvent.ResumePending -> {
                    listener?.onResumePending(e.paymentMethodInfo)
                }
                is CheckoutEvent.OnAdditionalInfoReceived -> {
                    listener?.onAdditionalInfoReceived(e.paymentMethodInfo)
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
        listener: PrimerCheckoutListener?
    ) {
        listener?.let { l -> setListener(l) }
        settings?.let {
            this.config = PrimerConfig(it)
        }
        addAnalyticsEvent(
            SdkFunctionParams(
                "configure",
                mapOf("settings" to this.config.toString())
            )
        )
    }

    override fun cleanup() {
        addAnalyticsEvent(SdkFunctionParams("cleanup"))
        GlobalConfigurationCacheDataSource.clear()
        listener = null
        subscription?.unregister(true)
        subscription = null
    }

    override fun showUniversalCheckout(context: Context, clientToken: String) {
        addAnalyticsEvent(SdkFunctionParams("showUniversalCheckout"))
        config.intent = PrimerIntent(PrimerSessionIntent.CHECKOUT)
        show(context, clientToken)
    }

    override fun showVaultManager(context: Context, clientToken: String) {
        addAnalyticsEvent(SdkFunctionParams("showVaultManager"))
        config.intent = PrimerIntent(PrimerSessionIntent.VAULT)
        show(context, clientToken)
    }

    override fun showPaymentMethod(
        context: Context,
        clientToken: String,
        paymentMethod: String,
        intent: PrimerSessionIntent
    ) {
        addAnalyticsEvent(
            SdkFunctionParams(
                "showPaymentMethod",
                mapOf("paymentMethodType" to paymentMethod, "intent" to intent.name)
            )
        )

        config.intent = PrimerIntent(intent, paymentMethod)
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
     */
    private fun show(context: Context, clientToken: String) {
        try {
            setupAndVerifyClientToken(clientToken)
            DISdkContext.sdkContainer?.let { sdkContainer ->
                sdkContainer.resolve<DefaultHeadlessManagerDelegate>().reset()
                sdkContainer.resolve<RawDataDelegate<*>>().reset()
                sdkContainer.resolve<RawDataDelegate<*>>(PaymentMethodType.PAYMENT_CARD.name)
                    .reset()
            }
            Intent(context, CheckoutSheetActivity::class.java)
                .apply {
                    putExtra(CheckoutSheetActivity.PRIMER_CONFIG_KEY, config)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
        } catch (expected: Exception) {
            Log.e("Primer", expected.message.toString())
            emitError(DefaultErrorMapper().getPrimerError(expected))
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
                sdkSessionId = SdkSessionDataSource.getSessionId(),
                sdkIntegrationType = config.settings.sdkIntegrationType,
                sdkPaymentHandling = config.settings.paymentHandling
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
