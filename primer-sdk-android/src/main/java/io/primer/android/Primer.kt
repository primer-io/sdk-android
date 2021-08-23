package io.primer.android

import android.content.Context
import android.content.Intent
import android.util.Log
import io.primer.android.data.tokenization.models.tokenizationSerializationModule
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.Serialization
import io.primer.android.model.UserDetails
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.ClientToken
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.model.dto.PaymentMethodTokenAdapter
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinApiExtension
import java.util.Locale

enum class UXMode {
    CHECKOUT,
    VAULT;

    val isNotVault: Boolean
        get() = this != VAULT
    val isVault: Boolean
        get() = this == VAULT
    val isCheckout: Boolean
        get() = this == CHECKOUT
}

@Deprecated("This object has been renamed to Primer.")
typealias UniversalCheckout = Primer

@Deprecated("This object has been renamed to PrimerTheme.")
typealias UniversalCheckoutTheme = PrimerTheme

object Primer {

    private lateinit var primer: InternalPrimer

    /**
     * Initializes the Primer SDK with the Application context and a client token Provider
     *
     * @param clientToken base64 string containing information about this Primer session.
     * It expires after 24 hours. Passing in an expired client token will throw an [IllegalArgumentException].
     */
    @Throws(IllegalArgumentException::class)
    fun initialize(
        context: Context,
        clientToken: String,
        locale: Locale = Locale.getDefault(),
        countryCode: CountryCode? = null,
        theme: PrimerTheme? = null,
    ) {

        val decodedToken: ClientToken = ClientToken.fromString(clientToken)

        // FIXME inject these dependencies
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Primer-SDK-Version", BuildConfig.SDK_VERSION_STRING)
                    .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
                    .addHeader("Primer-Client-Token", decodedToken.accessToken)
                    .build()
                    .let { chain.proceed(it) }
            }
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor { chain: Interceptor.Chain ->
                chain.request().newBuilder()
                    .url(chain.request().url().toString().replace("localhost", "10.0.2.2")).build()
                    .let { chain.proceed(it) }
            }
            .build()

        val json = Serialization.json

        val model = Model(decodedToken, okHttpClient, json)

        // we want to clear subscriptions
        if (::primer.isInitialized) {
            primer.unregisterSubscription()
        }

        primer = InternalPrimer(
            model,
            Dispatchers.IO,
            clientToken,
            locale,
            countryCode,
            theme,
        )
    }

    /**
     * Load the provided payment methods for use with the SDK
     */
    fun loadPaymentMethods(paymentMethods: List<PaymentMethod>) {
        primer.paymentMethods = paymentMethods
    }

    fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
        primer.getSavedPaymentMethods(callback)
    }

    fun showVault(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        webBrowserRedirectScheme: String? = null,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        preferWebView: Boolean = false,
        is3DSAtTokenizationEnabled: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        userDetails: UserDetails? = null,
        clearAllListeners: Boolean = false,
    ) {
        primer.showVault(
            context = context,
            listener = listener,
            amount = amount,
            currency = currency,
            webBrowserRedirectScheme = webBrowserRedirectScheme,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            doNotShowUi = doNotShowUi,
            preferWebView = preferWebView,
            clearAllListeners = clearAllListeners,
            is3DSAtTokenizationEnabled = is3DSAtTokenizationEnabled,
            debugOptions = debugOptions,
            orderId = orderId,
            userDetails = userDetails
        )
    }

    fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        webBrowserRedirectScheme: String? = null,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        preferWebView: Boolean = false,
        is3DSAtTokenizationEnabled: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        userDetails: UserDetails? = null,
        clearAllListeners: Boolean = false,
    ) {
        primer.showCheckout(
            context = context,
            listener = listener,
            amount = amount,
            currency = currency,
            webBrowserRedirectScheme = webBrowserRedirectScheme,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            doNotShowUi = doNotShowUi,
            preferWebView = preferWebView,
            is3DSAtTokenizationEnabled = is3DSAtTokenizationEnabled,
            debugOptions = debugOptions,
            clearAllListeners = clearAllListeners,
            orderId = orderId,
            userDetails = userDetails
        )
    }

    /**
     * Dismiss the checkout
     */
    fun dismiss(clearListeners: Boolean = false) {
        primer.dismiss()
        if (clearListeners) {
            primer.clearListener()
        }
    }

    /**
     * Toggle the loading screen
     */
    fun showProgressIndicator(visible: Boolean) {
        primer.showProgressIndicator(visible)
    }

    /**
     * Show a success screen then dismiss
     */
    fun showSuccess(autoDismissDelay: Int = 3000, successType: SuccessType = SuccessType.DEFAULT) {
        primer.showSuccess(autoDismissDelay, successType)
    }

    /**
     * Show a error screen then dismiss
     */
    fun showError(autoDismissDelay: Int = 3000, errorType: ErrorType = ErrorType.DEFAULT) {
        primer.showError(autoDismissDelay, errorType)
    }
}

internal class InternalPrimer constructor(
    private val model: Model,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fullToken: String,
    private val locale: Locale,
    private val countryCode: CountryCode? = null,
    private val theme: PrimerTheme? = null,
) {

    internal var paymentMethods: List<PaymentMethod> = emptyList()

    private var listener: CheckoutEventListener? = null
    private var subscription: EventBus.SubscriptionHandle? = null

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            if (e.public) {
                listener?.onCheckoutEvent(e)
            } else when (e) {
                is CheckoutEvent.ClearListeners -> {
                    clearListener()
                }
                else -> {
                }
            }
        }
    }

    fun clearListener() {
        listener = null
    }

    fun unregisterSubscription() {
        subscription?.unregister()
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
                            callback(
                                paymentMethodTokens.map {
                                    PaymentMethodTokenAdapter.internalToExternal(
                                        it
                                    )
                                }
                            )
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
    fun showVault(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        webBrowserRedirectScheme: String?,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        preferWebView: Boolean = false,
        is3DSAtTokenizationEnabled: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        userDetails: UserDetails? = null,
        clearAllListeners: Boolean,
    ) {
        show(
            context = context,
            listener = listener,
            uxMode = UXMode.VAULT,
            amount = amount,
            currency = currency,
            webBrowserRedirectScheme = webBrowserRedirectScheme,
            doNotShowUi = doNotShowUi,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            preferWebView = preferWebView,
            is3DSAtTokenizationEnabled = is3DSAtTokenizationEnabled,
            debugOptions = debugOptions,
            orderId = orderId,
            userDetails = userDetails,
            clearAllListeners = clearAllListeners,
        )
    }

    @KoinApiExtension
    fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        webBrowserRedirectScheme: String?,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
        preferWebView: Boolean = false,
        is3DSAtTokenizationEnabled: Boolean = false,
        debugOptions: PrimerDebugOptions? = null,
        orderId: String? = null,
        userDetails: UserDetails? = null,
        clearAllListeners: Boolean,
    ) {
        show(
            context = context,
            listener = listener,
            uxMode = UXMode.CHECKOUT,
            amount = amount,
            currency = currency,
            webBrowserRedirectScheme = webBrowserRedirectScheme,
            doNotShowUi = doNotShowUi,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            preferWebView = preferWebView,
            is3DSAtTokenizationEnabled = is3DSAtTokenizationEnabled,
            debugOptions = debugOptions,
            orderId = orderId,
            userDetails = userDetails,
            clearAllListeners = clearAllListeners,
        )
    }

    fun dismiss() {
        EventBus.broadcast(CheckoutEvent.DismissInternal(CheckoutExitReason.DISMISSED_BY_CLIENT))
    }

    fun showProgressIndicator(visible: Boolean) {
        EventBus.broadcast(CheckoutEvent.ToggleProgressIndicator(visible))
    }

    fun showSuccess(autoDismissDelay: Int = 3000, successType: SuccessType) {
        EventBus.broadcast(CheckoutEvent.ShowSuccess(autoDismissDelay, successType))
    }

    fun showError(autoDismissDelay: Int = 3000, errorType: ErrorType) {
        EventBus.broadcast(CheckoutEvent.ShowError(autoDismissDelay, errorType))
    }

    @Suppress("LongParameterList")
    @KoinApiExtension
    private fun show(
        context: Context,
        listener: CheckoutEventListener,
        uxMode: UXMode,
        amount: Int?,
        currency: String?,
        webBrowserRedirectScheme: String?,
        isStandalonePaymentMethod: Boolean,
        doNotShowUi: Boolean,
        preferWebView: Boolean,
        is3DSAtTokenizationEnabled: Boolean,
        debugOptions: PrimerDebugOptions?,
        orderId: String?,
        userDetails: UserDetails?,
        clearAllListeners: Boolean,
    ) {
        subscription?.unregister(clearAllListeners)

        this.listener = listener
        this.subscription = EventBus.subscribe(eventBusListener)

        val scheme = webBrowserRedirectScheme ?: context.packageName.let { "$it.primer" }

        WebviewInteropRegister.init(scheme)

        val config = CheckoutConfig(
            clientToken = fullToken,
            packageName = context.packageName,
            locale = locale,
            countryCode = countryCode,
            uxMode = uxMode,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            doNotShowUi = doNotShowUi,
            amount = amount,
            currency = currency,
            theme = theme,
            is3DSAtTokenizationEnabled = is3DSAtTokenizationEnabled,
            debugOptions = debugOptions,
            orderId = orderId,
            userDetails = userDetails,
            preferWebView = preferWebView,
        )

        try {
            Serialization.addModule(tokenizationSerializationModule)
            paymentMethods.forEach { Serialization.addModule(it.serializersModule) }

            val json = Serialization.json

            Intent(context, CheckoutSheetActivity::class.java)
                .apply {
                    putExtra(
                        "config",
                        json.encodeToString(CheckoutConfig.serializer(), config),
                    )
                    putExtra(
                        "paymentMethods",
                        json.encodeToString(serializer(), paymentMethods),
                    )
                }
                .run { context.startActivity(this) }
        } catch (e: Exception) {
            Log.e("Primer", e.message.toString())
            val apiError = APIError("View failed to load.")
            EventBus.broadcast(CheckoutEvent.ApiError(apiError))
        }
    }
}

interface CheckoutEventListener {

    fun onCheckoutEvent(e: CheckoutEvent)
}
