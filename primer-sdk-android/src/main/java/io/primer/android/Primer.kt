package io.primer.android

import android.content.Context
import android.content.Intent
import android.util.Log
import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.SdkSessionDataSource
import io.primer.android.analytics.data.models.AnalyticsSdkFunctionEventRequest
import io.primer.android.analytics.data.models.FunctionProperties
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.data.tokenization.models.tokenizationSerializationModule
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.Serialization
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethod
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.Customer
import io.primer.android.data.token.model.ClientToken
import io.primer.android.events.EventDispatcher
import io.primer.android.model.dto.PrimerIntent
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.serialization.encodeToString
import java.util.Locale

@Deprecated("This object has been renamed to Primer.")
val UniversalCheckout: PrimerInterface = Primer.instance

@Deprecated("This object has been renamed to PrimerTheme.")
typealias UniversalCheckoutTheme = PrimerTheme

class Primer private constructor() : PrimerInterface {

    internal var paymentMethods: MutableList<PaymentMethod> = mutableListOf()
    private var listener: CheckoutEventListener? = null
    private var config: PrimerConfig = PrimerConfig()
    private var subscription: EventBus.SubscriptionHandle? = null

    private val eventDispatcher = EventDispatcher()

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            if (e.public) {
                // for backward compatibility, call separate flow onClientSessionActions
                addAnalyticsEvent(SdkFunctionParams("onEvent", mapOf("event" to e.type.name)))
                if (e is CheckoutEvent.OnClientSessionActions) {
                    listener?.onClientSessionActions(e)
                } else {
                    listener?.onCheckoutEvent(e)
                }
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun configure(
        config: PrimerConfig?,
        listener: CheckoutEventListener?,
    ) {
        listener?.let { l -> setListener(l) }
        config?.let {
            this.config = config
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
    private fun setListener(listener: CheckoutEventListener) {
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
        if (clientToken.isBlank()) {
            Log.e("Primer SDK", "client token not provided")
            eventDispatcher.dispatchEvent(
                CheckoutEvent.ApiError(APIError("Client token not provided."))
            )
            return
        }

        setupAndVerifyClientToken(clientToken)

        val scheme =
            config.settings.options.redirectScheme ?: context.packageName.let { "$it.primer" }
        WebviewInteropRegister.init(scheme)

        try {
            // TODO: refactor the way we pass in the config.
            Serialization.addModule(tokenizationSerializationModule)
            val encodedConfig = Serialization.json.encodeToString(PrimerConfig.serializer(), config)
            Intent(context, CheckoutSheetActivity::class.java)
                .apply { putExtra("config", encodedConfig) }
                .run { context.startActivity(this) }
        } catch (e: Exception) {
            Log.e("Primer", e.message.toString())
            val message = """
                Failed to configure Primer SDK due to serialization exception. 
                Please raise an issue in the SDK's public repository:
                https://github.com/primer-io/primer-sdk-android
            """.trimIndent()
            val apiError = APIError(message)
            eventDispatcher.dispatchEvent(CheckoutEvent.ApiError(apiError))
        }
    }

    override fun dismiss(clearListeners: Boolean) {
        addAnalyticsEvent(SdkFunctionParams("dismiss"))
        if (clearListeners) listener = null
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_CLIENT))
    }

    override fun showSuccess(autoDismissDelay: Int, successType: SuccessType) {
        addAnalyticsEvent(SdkFunctionParams("showSuccess"))
        EventBus.broadcast(CheckoutEvent.ShowSuccess(autoDismissDelay, successType))
    }

    override fun showError(autoDismissDelay: Int, errorType: ErrorType) {
        addAnalyticsEvent(SdkFunctionParams("showError"))
        EventBus.broadcast(CheckoutEvent.ShowError(autoDismissDelay, errorType))
    }

    /**
     *
     *
     *
     * Deprecated methods
     *
     *
     *
     */
    override fun initialize(
        context: Context,
        clientToken: String,
        locale: Locale,
        countryCode: CountryCode?,
        theme: PrimerTheme?
    ) {
        setupAndVerifyClientToken(clientToken)
        config.theme = theme ?: PrimerTheme.create()
        config.settings.options.locale = locale
        config.settings.order.countryCode = countryCode

        // we want to clear subscriptions
        subscription?.unregister()
    }

    override fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
        this.paymentMethods = paymentMethods.toMutableList()
    }

    override fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int?,
        currency: String?,
        webBrowserRedirectScheme: String?,
        isStandalonePaymentMethod: Boolean,
        doNotShowUi: Boolean,
        preferWebView: Boolean,
        debugOptions: PrimerDebugOptions?,
        orderId: String?,
        customer: Customer?,
        clearAllListeners: Boolean,
    ) {
        config.settings.order.amount = amount
        config.settings.order.currency = currency
        config.intent = PrimerIntent.build(false, paymentMethods)
        config.settings.options.redirectScheme = webBrowserRedirectScheme
        config.settings.options.showUI = doNotShowUi.not()
        config.settings.options.preferWebView = preferWebView
        // 3DS
        config.settings.options.debugOptions = debugOptions
        config.settings.order.id = orderId
        customer?.let { config.settings.customer = customer }

        setListener(listener)
        paymentMethods.find { it is Klarna }?.also {
            if (it is Klarna) {
                config.settings.order.description = it.orderDescription
                config.settings.order.items = it.orderItems
                config.settings.options.klarnaWebViewTitle = it.webViewTitle
            }
        }
        paymentMethods.find { it is Apaya }?.also {
            if (it is Apaya) {
                config.settings.options.apayaWebViewTitle = it.webViewTitle
                config.settings.customer =
                    config.settings.customer.copy(mobilePhone = it.mobilePhone)
            }
        }
        paymentMethods.find { it is GooglePay }?.also {
            if (it is GooglePay) {
                config.settings.business = config.settings.business.copy(name = it.merchantName)
                config.settings.options.googlePayAllowedCardNetworks = it.allowedCardNetworks
                config.settings.options.googlePayButtonStyle = it.buttonStyle
            }
        }
        paymentMethods.find { it is GoCardless }?.also {
            if (it is GoCardless) {
                config.settings.customer = config.settings.customer.copy(
                    firstName = it.customerName,
                    email = it.customerEmail
                )
            }
        }
        show(context, config.clientTokenBase64.orEmpty())
    }

    override fun showVault(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int?,
        currency: String?,
        webBrowserRedirectScheme: String?,
        isStandalonePaymentMethod: Boolean,
        doNotShowUi: Boolean,
        preferWebView: Boolean,
        is3DSOnVaultingEnabled: Boolean,
        debugOptions: PrimerDebugOptions?,
        orderId: String?,
        customer: Customer?,
        clearAllListeners: Boolean,
    ) {
        config.settings.order.amount = amount
        config.settings.order.currency = currency
        config.intent = PrimerIntent.build(true, paymentMethods)
        config.settings.options.redirectScheme = webBrowserRedirectScheme
        config.settings.options.showUI = doNotShowUi.not()
        config.settings.options.preferWebView = preferWebView
        // 3DS
        config.settings.options.is3DSOnVaultingEnabled = is3DSOnVaultingEnabled
        config.settings.options.debugOptions = debugOptions
        config.settings.order.id = orderId
        customer?.let { config.settings.customer = customer }

        setListener(listener)
        paymentMethods.find { it is Klarna }?.also {
            if (it is Klarna) {
                config.settings.order.description = it.orderDescription
                config.settings.order.items = it.orderItems
                config.settings.options.klarnaWebViewTitle = it.webViewTitle
            }
        }
        paymentMethods.find { it is Apaya }?.also {
            if (it is Apaya) {
                config.settings.options.apayaWebViewTitle = it.webViewTitle
                config.settings.customer =
                    config.settings.customer.copy(mobilePhone = it.mobilePhone)
            }
        }
        paymentMethods.find { it is GooglePay }?.also {
            if (it is GooglePay) {
                config.settings.business = config.settings.business.copy(name = it.merchantName)
                config.settings.options.googlePayAllowedCardNetworks = it.allowedCardNetworks
                config.settings.options.googlePayButtonStyle = it.buttonStyle
            }
        }
        paymentMethods.find { it is GoCardless }?.also {
            if (it is GoCardless) {
                config.settings.customer = config.settings.customer.copy(
                    firstName = it.customerName,
                    email = it.customerEmail
                )
            }
        }
        show(context, config.clientTokenBase64.orEmpty())
    }

    override fun showProgressIndicator(visible: Boolean) {
        EventBus.broadcast(CheckoutEvent.ToggleProgressIndicator(visible))
    }

    private fun setupAndVerifyClientToken(clientToken: String) {
        ClientToken.fromString(clientToken)
        this.config.clientTokenBase64 = clientToken
    }

    internal fun addAnalyticsEvent(params: SdkFunctionParams) {
        LocalAnalyticsDataSource.instance.addEvent(
            AnalyticsSdkFunctionEventRequest(
                properties = FunctionProperties(params.name, params.params),
                sdkSessionId = SdkSessionDataSource.getSessionId()
            )
        )
    }

    companion object {

        /**
         * Singleton instance of [Primer]. Use this to call SDK methods.
         */
        @JvmStatic
        val instance: PrimerInterface by lazy { Primer() }
    }
}
