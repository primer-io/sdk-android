package io.primer.android.components

import android.content.Context
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.DefaultRawDataManagerDelegate
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import org.koin.core.component.get

class PrimerHeadlessUniversalCheckout private constructor() :
    PrimerHeadlessUniversalCheckoutInterface, DIAppComponent {

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            headlessUniversalCheckout?.addAnalyticsEvent(
                SdkFunctionParams(
                    object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                    mapOf("event" to e.type.name)
                )
            )
            when (e) {
                is CheckoutEvent.TokenizationSuccessHUC -> {
                    checkoutListener?.onTokenizeSuccess(
                        e.data, e.resumeHandler
                    )
                }
                is CheckoutEvent.TokenizationStarted ->
                    checkoutListener?.onTokenizationStarted(e.paymentMethodType)
                is CheckoutEvent.ConfigurationSuccess -> {
                    checkoutListener?.onAvailablePaymentMethodsLoaded(e.paymentMethods)
                }
                is CheckoutEvent.PreparationStarted ->
                    uiListener?.onPreparationStarted(e.paymentMethodType)
                is CheckoutEvent.PaymentMethodPresented -> uiListener?.onPaymentMethodShowed(
                    e.paymentMethodType
                )
                is CheckoutEvent.ResumeSuccessHUC -> checkoutListener?.onCheckoutResume(
                    e.resumeToken,
                    e.resumeHandler
                )
                is CheckoutEvent.ResumePending ->
                    checkoutListener?.onResumePending(e.paymentMethodInfo)
                is CheckoutEvent.OnAdditionalInfoReceived ->
                    checkoutListener?.onCheckoutAdditionalInfoReceived(e.paymentMethodInfo)
                is CheckoutEvent.PaymentCreateStartedHUC -> {
                    checkoutListener?.onBeforePaymentCreated(e.data, e.createPaymentHandler)
                }
                is CheckoutEvent.PaymentSuccess -> {
                    checkoutListener?.onCheckoutCompleted(e.data)
                }
                is CheckoutEvent.CheckoutPaymentError -> {
                    checkoutListener?.onFailed(e.error, e.data)
                }
                is CheckoutEvent.CheckoutError -> {
                    checkoutListener?.onFailed(e.error)
                }
                is CheckoutEvent.ClientSessionUpdateSuccess -> {
                    checkoutListener?.onClientSessionUpdated(e.data)
                }
                is CheckoutEvent.ClientSessionUpdateStarted -> {
                    checkoutListener?.onBeforeClientSessionUpdated()
                }
                else -> Unit
            }
        }
    }

    private var config: PrimerConfig? = null
    private var headlessUniversalCheckout: DefaultHeadlessUniversalCheckoutDelegate? = null
    private var checkoutListener: PrimerHeadlessUniversalCheckoutListener? = null
    private var uiListener: PrimerHeadlessUniversalCheckoutUiListener? = null
    private var subscription: EventBus.SubscriptionHandle? = null

    override fun setCheckoutListener(
        listener: PrimerHeadlessUniversalCheckoutListener
    ) {
        headlessUniversalCheckout?.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty()
            )
        )
        subscription?.unregister()
        subscription = EventBus.subscribe(eventBusListener)
        this.checkoutListener = listener
    }

    override fun setCheckoutUiListener(uiListener: PrimerHeadlessUniversalCheckoutUiListener) {
        headlessUniversalCheckout?.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty()
            )
        )
        this.uiListener = uiListener
    }

    override fun start(
        context: Context,
        clientToken: String,
        settings: PrimerSettings?,
        checkoutListener: PrimerHeadlessUniversalCheckoutListener?,
        uiListener: PrimerHeadlessUniversalCheckoutUiListener?
    ) {
        checkoutListener?.let { setCheckoutListener(it) }
        uiListener?.let { setCheckoutUiListener(it) }
        try {
            initialize(context, clientToken, settings)
            headlessUniversalCheckout?.start() ?: run {
                emitError(HUCError.InitializationError(INITIALIZATION_ERROR))
            }
            headlessUniversalCheckout?.addAnalyticsEvent(
                SdkFunctionParams(
                    object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                    mapOf(
                        "settings" to this.config?.settings.toString(),
                        "clientToken" to clientToken
                    )
                )
            )
        } catch (expected: Exception) {
            emitError(DefaultErrorMapper().getPrimerError(expected))
        }
    }

    override fun cleanup() {
        headlessUniversalCheckout?.addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty()
            )
        )
        headlessUniversalCheckout?.clear(null)
        headlessUniversalCheckout = null
        checkoutListener = null
        uiListener = null
        subscription?.unregister()
        subscription = null
        get<DefaultHeadlessManagerDelegate>().reset()
        get<DefaultRawDataManagerDelegate>().reset()
    }

    internal fun emitError(error: PrimerError) {
        when (getConfig().settings.paymentHandling) {
            PrimerPaymentHandling.AUTO -> checkoutListener?.onFailed(
                CheckoutEvent.CheckoutPaymentError(
                    error
                ).error,
                null
            )
            PrimerPaymentHandling.MANUAL -> checkoutListener?.onFailed(
                CheckoutEvent.CheckoutError(
                    error
                ).error
            )
        }
    }

    internal fun addAnalyticsEvent(params: BaseAnalyticsParams) {
        headlessUniversalCheckout?.addAnalyticsEvent(params)
    }

    private fun initialize(context: Context, clientToken: String, settings: PrimerSettings?) {
        val newConfig = settings?.let { PrimerConfig(it) } ?: getConfig()
        this.config = newConfig
        verifyClientToken(clientToken)
        newConfig.clientTokenBase64 = clientToken
        newConfig.settings.fromHUC = true
        setupDI(context, newConfig)
    }

    private fun getConfig() = config ?: PrimerConfig()

    private fun setupDI(context: Context, config: PrimerConfig) {
        DIAppContext.app?.let {
            get<DefaultHeadlessManagerDelegate>().reset()
            get<DefaultRawDataManagerDelegate>().reset()
            it.close()
        }
        DIAppContext.init(context.applicationContext, config)
        // refresh the instances
        headlessUniversalCheckout = get()
    }

    private fun verifyClientToken(clientToken: String) = ClientToken.fromString(clientToken)

    companion object {

        private const val INITIALIZATION_ERROR =
            "PrimerHeadlessUniversalCheckout is not initialized properly."

        internal val instance by lazy { PrimerHeadlessUniversalCheckout() }

        @JvmStatic
        val current = instance as PrimerHeadlessUniversalCheckoutInterface
    }
}
