package io.primer.android.components

import android.content.Context
import android.view.View
import io.primer.android.CheckoutEventListener
import io.primer.android.PaymentMethodIntent
import io.primer.android.Primer
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutInputData
import io.primer.android.components.presentation.HeadlessUniversalCheckoutViewModel
import io.primer.android.components.ui.assets.Brand
import io.primer.android.components.ui.assets.ImageType
import io.primer.android.components.ui.assets.PrimerAssetManager
import io.primer.android.components.ui.navigation.Navigator
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.components.ui.widgets.elements.PrimerInputElementType
import io.primer.android.data.tokenization.models.tokenizationSerializationModule
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.Serialization
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.PrimerPaymentMethodType
import io.primer.android.model.dto.toPrimerPaymentMethod
import org.koin.core.component.get

class PrimerHeadlessUniversalCheckout private constructor() :
    PrimerHeadlessUniversalCheckoutInterface, CheckoutEventListener, DIAppComponent {

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            when (e) {
                is CheckoutEvent.TokenizationSuccess -> componentsListener?.onTokenizationSuccess(
                    e.data,
                    e.resumeHandler
                )
                is CheckoutEvent.TokenizationStarted -> componentsListener?.onTokenizationStarted()
                is CheckoutEvent.ConfigurationSuccess ->
                    componentsListener?.onClientSessionSetupSuccessfully(e.paymentMethods)
                is CheckoutEvent.PreparationStarted ->
                    componentsListener?.onTokenizationPreparation()
                is CheckoutEvent.PaymentMethodPresented ->
                    componentsListener?.onPaymentMethodShowed()
                is CheckoutEvent.Start3DS -> navigator?.openThreeDsScreen()
                is CheckoutEvent.StartAsyncRedirectFlow -> navigator?.openAsyncWebViewScreen(
                    e.title,
                    e.redirectUrl,
                    e.statusUrl
                )
                is CheckoutEvent.ResumeSuccess ->
                    componentsListener?.onResumeSuccess(e.resumeToken, e.resumeHandler)
                is CheckoutEvent.ApiError -> componentsListener?.onError(e.data)
                is CheckoutEvent.TokenizationError -> componentsListener?.onError(e.data)
                is CheckoutEvent.ResumeError -> componentsListener?.onError(e.data)
                is CheckoutEvent.Exit -> {
                    if (e.data.reason == CheckoutExitReason.DISMISSED_BY_USER) {
                        componentsListener?.onError(
                            APIError.createDefaultWithMessage(
                                CANCELLATION_MESSAGE
                            )
                        )
                    }
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
        config: PrimerConfig?,
        listener: PrimerHeadlessUniversalCheckoutListener?
    ) {
        listener?.let { setListener(it) }
        initialize(context, clientToken, config)
        viewModel?.start() ?: run { componentsListener?.onError(APIError(INITIALIZATION_ERROR)) }
    }

    override fun listRequiredInputElementTypes(paymentMethodType: PrimerPaymentMethodType):
        List<PrimerInputElementType>? {
        if (viewModel == null) componentsListener?.onError(APIError(INITIALIZATION_ERROR))
        return viewModel?.listRequiredInputElementTypes(paymentMethodType)
    }

    override fun makeView(
        paymentMethodType: PrimerPaymentMethodType
    ): View? {
        if (paymentMethodViewFactory == null) componentsListener?.onError(
            APIError(
                INITIALIZATION_ERROR
            )
        )
        return paymentMethodViewFactory?.getViewForPaymentMethod(paymentMethodType)
    }

    override fun showPaymentMethod(
        context: Context,
        paymentMethod: PrimerPaymentMethodType
    ) {
        val config = getConfig()
        val options = config.settings.options.copy(showUI = false)
        val primer = Primer.instance
        primer.configure(
            config = config.copy(settings = config.settings.copy(options = options)),
            listener = this
        )
        // we need to register again
        subscription?.unregister()
        subscription = EventBus.subscribe(eventBusListener)
        primer.showPaymentMethod(
            context,
            config.clientTokenBase64.orEmpty(),
            paymentMethod.toPrimerPaymentMethod(),
            PaymentMethodIntent.CHECKOUT
        )
    }

    override fun cleanup() {
        viewModel?.clear()
        componentsListener = null
        subscription?.unregister()
        subscription = null
    }

    override fun onCheckoutEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenizationSuccess,
            is CheckoutEvent.TokenizationError,
            is CheckoutEvent.Exit -> {
                Primer.instance.dismiss()
            }
            else -> Unit
        }
    }

    internal fun startTokenization(
        paymentMethodType: PrimerPaymentMethodType,
        inputData: PrimerHeadlessUniversalCheckoutInputData
    ) = viewModel?.startTokenization(paymentMethodType, inputData)

    private fun initialize(context: Context, clientToken: String, config: PrimerConfig?) {
        val newConfig = config ?: getConfig()
        newConfig.clientTokenBase64 = clientToken
        this.config = newConfig
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

    companion object {

        private const val INITIALIZATION_ERROR = "PrimerHeadlessUniversalCheckout is not" +
            " initialized properly. Please ensure you are calling start method."
        private const val CANCELLATION_MESSAGE = "Payment method cancelled."

        internal val instance by lazy { PrimerHeadlessUniversalCheckout() }

        @JvmStatic
        val current = instance as PrimerHeadlessUniversalCheckoutInterface

        fun getAsset(
            context: Context,
            brand: Brand,
            assetType: ImageType,
        ): Int? {
            return PrimerAssetManager.getAsset(context, brand, assetType)
        }
    }
}
