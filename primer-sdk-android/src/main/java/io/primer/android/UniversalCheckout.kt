package io.primer.android

import android.content.Context
import android.content.Intent
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Model
import io.primer.android.model.OperationResult
import io.primer.android.model.Serialization
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

object UniversalCheckout {

    private lateinit var checkout: InternalUniversalCheckout

    /**
     * Initializes the Primer SDK with the Application context and a client token Provider
     */
    fun initialize(
        context: Context,
        clientToken: String,
        locale: Locale = Locale.getDefault(),
        countryCode: CountryCode? = null,
        theme: UniversalCheckoutTheme? = null,
    ) {
        val decodedToken = ClientToken.fromString(clientToken)

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
            .build()

        val json = Serialization.json

        val model = Model(decodedToken, okHttpClient, json)

        checkout = InternalUniversalCheckout(
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
        checkout.paymentMethods = paymentMethods
    }

    fun getSavedPaymentMethods(callback: (List<PaymentMethodToken>) -> Unit) {
        checkout.getSavedPaymentMethods(callback)
    }

    fun showVault(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        customScheme: String? = null,
        customHost: String? = null,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
    ) {
        checkout.showVault(
            context = context,
            listener = listener,
            amount = amount,
            currency = currency,
            customScheme = customScheme,
            customHost = customHost,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            doNotShowUi = doNotShowUi
        )
    }

    fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        customScheme: String? = null,
        customHost: String? = null,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
    ) {
        checkout.showCheckout(
            context = context,
            listener = listener,
            amount = amount,
            currency = currency,
            customScheme = customScheme,
            customHost = customHost,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
            doNotShowUi = doNotShowUi
        )
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
    fun showSuccess(autoDismissDelay: Int = 3000, successType: SuccessType = SuccessType.DEFAULT) {
        checkout.showSuccess(autoDismissDelay, successType)
    }

    /**
     * Show a error screen then dismiss
     */
    fun showError(autoDismissDelay: Int = 3000, errorType: ErrorType = ErrorType.DEFAULT) {
        checkout.showError(autoDismissDelay, errorType)
    }
}

internal class InternalUniversalCheckout constructor(
    private val model: Model,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fullToken: String,
    private val locale: Locale,
    private val countryCode: CountryCode? = null,
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
        customScheme: String?,
        customHost: String?,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
    ) {
        show(
            context = context,
            listener = listener,
            uxMode = UXMode.VAULT,
            amount = amount,
            currency = currency,
            customScheme = customScheme,
            customHost = customHost,
            doNotShowUi = doNotShowUi,
            isStandalonePaymentMethod = isStandalonePaymentMethod
        )
    }

    @KoinApiExtension
    fun showCheckout(
        context: Context,
        listener: CheckoutEventListener,
        amount: Int? = null,
        currency: String? = null,
        customScheme: String?,
        customHost: String?,
        isStandalonePaymentMethod: Boolean = false,
        doNotShowUi: Boolean = false,
    ) {
        show(
            context = context,
            listener = listener,
            uxMode = UXMode.CHECKOUT,
            amount = amount,
            currency = currency,
            customScheme = customScheme,
            customHost = customHost,
            doNotShowUi = doNotShowUi,
            isStandalonePaymentMethod = isStandalonePaymentMethod,
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
        customScheme: String?,
        customHost: String?,
        isStandalonePaymentMethod: Boolean,
        doNotShowUi: Boolean,
    ) {
        subscription?.unregister()

        this.listener = listener
        this.subscription = EventBus.subscribe(eventBusListener)

        val scheme = context.packageName.let {
            context.packageName + ".primer"
        }

        val host = customHost ?: "klarna" // default to klarna for now

        WebviewInteropRegister.init(scheme, host)

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
        )

        paymentMethods.forEach { Serialization.addModule(it.serializersModule) }
        val json = Serialization.json

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
