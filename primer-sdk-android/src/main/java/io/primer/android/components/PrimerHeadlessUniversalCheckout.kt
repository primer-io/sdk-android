package io.primer.android.components

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.Primer
import io.primer.android.PrimerCheckoutListener
import io.primer.android.PrimerSessionIntent
import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.presentation.HeadlessUniversalCheckoutViewModel
import io.primer.android.components.ui.assets.ImageType
import io.primer.android.components.ui.assets.PrimerAssetManager
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.tokenization.models.tokenizationSerializationModule
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Serialization
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.token.model.ClientToken
import org.koin.core.component.get

@ExperimentalPrimerApi
class PrimerHeadlessUniversalCheckout private constructor() :
    PrimerHeadlessUniversalCheckoutInterface, PrimerCheckoutListener, DIAppComponent {

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            when (e) {
                is CheckoutEvent.TokenizationSuccess -> {
                    componentsListener?.onTokenizeSuccess(
                        e.data,
                        e.resumeHandler
                    )
                }
                is CheckoutEvent.TokenizationStarted ->
                    componentsListener?.onTokenizationStarted(e.paymentMethodType)
                is CheckoutEvent.ConfigurationSuccess ->
                    componentsListener?.onAvailablePaymentMethodsLoaded(e.paymentMethods)
                is CheckoutEvent.PreparationStarted ->
                    componentsListener?.onPreparationStarted(e.paymentMethodType)
                is CheckoutEvent.PaymentMethodPresented ->
                    componentsListener?.onPaymentMethodShowed(e.paymentMethodType)
                is CheckoutEvent.Start3DS -> {
                    if (e.processor3DSData == null) navigator?.openThreeDsScreen()
                    else navigator?.openAsyncWebViewScreen(
                        e.processor3DSData.title,
                        e.processor3DSData.paymentMethodType,
                        e.processor3DSData.redirectUrl,
                        e.processor3DSData.statusUrl
                    )
                }
                is CheckoutEvent.StartAsyncRedirectFlow -> navigator?.openAsyncWebViewScreen(
                    e.title,
                    e.paymentMethodType,
                    e.redirectUrl,
                    e.statusUrl
                )
                is CheckoutEvent.ResumeSuccess ->
                    componentsListener?.onResumeSuccess(e.resumeToken, e.resumeHandler)

                is CheckoutEvent.PaymentCreateStartedHUC -> {
                    componentsListener?.onBeforePaymentCreated(e.data, e.createPaymentHandler)
                }

                is CheckoutEvent.PaymentContinueHUC -> {
                    Primer.instance.dismiss()
                    viewModel?.createPayment(
                        e.data.token,
                        e.resumeHandler
                    )
                }
                is CheckoutEvent.PaymentSuccess -> {
                    componentsListener?.onCheckoutCompleted(e.data)
                }
                is CheckoutEvent.CheckoutPaymentError -> {
                    componentsListener?.onFailed(e.error, e.data)
                }
                is CheckoutEvent.CheckoutError -> {
                    componentsListener?.onFailed(e.error)
                }
                is CheckoutEvent.ResumeSuccessInternal -> viewModel?.resumePayment(
                    e.resumeToken,
                    e.resumeHandler
                )
                is CheckoutEvent.ClientSessionUpdateSuccess -> {
                    componentsListener?.onClientSessionUpdated(e.data)
                }
                is CheckoutEvent.ClientSessionUpdateStarted -> {
                    componentsListener?.onBeforeClientSessionUpdated()
                }
                else -> Unit
            }
        }
    }

    private var config: PrimerConfig? = null
    private var navigator: Navigator? = null
    private var paymentMethodViewFactory: PrimerPaymentMethodViewFactory? = null
    private var viewModel: HeadlessUniversalCheckoutViewModel? = null
    private var componentsListener: PrimerHeadlessUniversalCheckoutListener? = null
    private var subscription: EventBus.SubscriptionHandle? = null

    override fun setListener(listener: PrimerHeadlessUniversalCheckoutListener) {
        subscription?.unregister()
        subscription = EventBus.subscribe(eventBusListener)
        this.componentsListener = listener
    }

    override fun start(
        context: Context,
        clientToken: String,
        settings: PrimerSettings?,
        listener: PrimerHeadlessUniversalCheckoutListener?
    ) {
        listener?.let { setListener(it) }
        try {
            initialize(context, clientToken, settings)
            viewModel?.start()
                ?: run {
                    emitError(HUCError.InitializationError(INITIALIZATION_ERROR))
                }
        } catch (expected: Exception) {
            emitError(DefaultErrorMapper().getPrimerError(expected))
        }
    }

    override fun listRequiredInputElementTypes(paymentMethodType: PrimerPaymentMethodType):
        List<PrimerInputElementType>? {
        if (viewModel == null) {
            emitError(HUCError.InitializationError(INITIALIZATION_ERROR))
        }
        return viewModel?.listRequiredInputElementTypes(paymentMethodType)
    }

    override fun makeView(
        paymentMethodType: PrimerPaymentMethodType
    ): View? {
        if (paymentMethodViewFactory == null) {
            emitError(HUCError.InitializationError(INITIALIZATION_ERROR))
        }
        return paymentMethodViewFactory?.getViewForPaymentMethod(paymentMethodType)
    }

    override fun showPaymentMethod(
        context: Context,
        paymentMethodType: PrimerPaymentMethodType
    ) {
        val config = getConfig()
        config.settings.fromHUC = true
        val primer = Primer.instance
        primer.configure(
            config.settings,
            this
        )
        // we need to register again
        subscription?.unregister()
        subscription = EventBus.subscribe(eventBusListener)
        primer.showPaymentMethod(
            context,
            config.clientTokenBase64.orEmpty(),
            paymentMethodType,
            PrimerSessionIntent.CHECKOUT
        )
    }

    override fun cleanup() {
        viewModel?.clear()
        viewModel = null
        componentsListener = null
        subscription?.unregister()
        subscription = null
    }

    override fun onTokenizeSuccess(
        paymentMethodTokenData: PrimerPaymentMethodTokenData,
        decisionHandler: PrimerResumeDecisionHandler
    ) {
        super.onTokenizeSuccess(paymentMethodTokenData, decisionHandler)
        Primer.instance.dismiss(true)
    }

    override fun onFailed(error: PrimerError, errorHandler: PrimerErrorDecisionHandler?) {
        Primer.instance.dismiss(true)
    }

    override fun onFailed(
        error: PrimerError,
        checkoutData: PrimerCheckoutData?,
        errorHandler: PrimerErrorDecisionHandler?
    ) {
        Primer.instance.dismiss(true)
    }

    override fun onDismissed() {
        Primer.instance.dismiss(true)
    }

    override fun onCheckoutCompleted(checkoutData: PrimerCheckoutData) = Unit

    internal fun startTokenization(
        paymentMethodType: PrimerPaymentMethodType,
        inputData: PrimerHeadlessUniversalCheckoutInputData
    ) = viewModel?.dispatchAction(paymentMethodType, inputData) { error ->
        if (error == null) viewModel?.startTokenization(paymentMethodType, inputData)
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
        DIAppContext.app?.close()
        DIAppContext.init(context.applicationContext, config)
        Serialization.addModule(tokenizationSerializationModule)
        // refresh the instances
        viewModel = get()
        navigator = get()
        paymentMethodViewFactory = get()
    }

    private fun verifyClientToken(clientToken: String) = ClientToken.fromString(clientToken)

    private fun emitError(error: PrimerError) {
        when (getConfig().settings.paymentHandling) {
            PrimerPaymentHandling.AUTO -> componentsListener?.onFailed(
                CheckoutEvent.CheckoutPaymentError(
                    error
                ).error,
                null
            )
            PrimerPaymentHandling.MANUAL -> componentsListener?.onFailed(
                CheckoutEvent.CheckoutError(
                    error
                ).error
            )
        }
    }

    companion object {

        private const val INITIALIZATION_ERROR = "PrimerHeadlessUniversalCheckout is not" +
            " initialized properly."

        internal val instance by lazy { PrimerHeadlessUniversalCheckout() }

        @JvmStatic
        val current = instance as PrimerHeadlessUniversalCheckoutInterface

        @DrawableRes
        fun getAsset(
            paymentMethodType: PrimerPaymentMethodType,
            assetType: ImageType,
        ): Int? {
            return PrimerAssetManager.getAsset(paymentMethodType, assetType)
        }
    }
}
