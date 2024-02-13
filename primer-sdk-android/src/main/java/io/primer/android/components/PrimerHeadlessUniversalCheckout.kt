package io.primer.android.components

import android.content.Context
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.components.presentation.paymentMethods.base.DefaultHeadlessManagerDelegate
import io.primer.android.components.presentation.paymentMethods.raw.RawDataDelegate
import io.primer.android.components.presentation.vault.VaultManagerDelegate
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.model.ClientToken
import io.primer.android.di.DISdkComponent
import io.primer.android.di.DISdkContext
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus

class PrimerHeadlessUniversalCheckout private constructor() :
    PrimerHeadlessUniversalCheckoutInterface, DISdkComponent {

    private val eventBusListener = object : EventBus.EventListener {
        override fun onEvent(e: CheckoutEvent) {
            when (e) {
                is CheckoutEvent.TokenizationSuccessHUC -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_TOKENIZE_SUCCESS
                        )
                    )
                    checkoutListener?.onTokenizeSuccess(
                        e.data,
                        e.resumeHandler
                    )
                }

                is CheckoutEvent.TokenizationStarted -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_TOKENIZATION_STARTED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                                    to e.paymentMethodType
                            )
                        )
                    )
                    checkoutListener?.onTokenizationStarted(e.paymentMethodType)
                }

                is CheckoutEvent.ConfigurationSuccess -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants
                                .ON_AVAILABLE_PAYMENT_METHODS_LOADED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants
                                    .AVAILABLE_PAYMENT_METHODS_PARAM to e.paymentMethods.toString()
                            )
                        )
                    )
                    checkoutListener?.onAvailablePaymentMethodsLoaded(e.paymentMethods)
                }

                is CheckoutEvent.PreparationStarted -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_PREPARATION_STARTED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                                    to e.paymentMethodType
                            )
                        )
                    )
                    uiListener?.onPreparationStarted(e.paymentMethodType)
                }

                is CheckoutEvent.PaymentMethodPresented -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_PAYMENT_METHOD_SHOWED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants.PAYMENT_METHOD_TYPE
                                    to e.paymentMethodType
                            )
                        )
                    )
                    uiListener?.onPaymentMethodShowed(
                        e.paymentMethodType
                    )
                }

                is CheckoutEvent.ResumeSuccessHUC -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_RESUME
                        )
                    )
                    checkoutListener?.onCheckoutResume(
                        e.resumeToken,
                        e.resumeHandler
                    )
                }

                is CheckoutEvent.ResumePending -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_PENDING
                        )
                    )
                    checkoutListener?.onResumePending(e.paymentMethodInfo)
                }

                is CheckoutEvent.OnAdditionalInfoReceived -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants
                                .ON_CHECKOUT_ADDITIONAL_INFO_RECEIVED
                        )
                    )
                    checkoutListener?.onCheckoutAdditionalInfoReceived(e.paymentMethodInfo)
                }

                is CheckoutEvent.PaymentCreateStartedHUC -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_BEFORE_PAYMENT_COMPLETED
                        )
                    )
                    checkoutListener?.onBeforePaymentCreated(e.data, e.createPaymentHandler)
                }

                is CheckoutEvent.PaymentSuccess -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_COMPLETED
                        )
                    )
                    checkoutListener?.onCheckoutCompleted(e.data)
                }

                is CheckoutEvent.CheckoutPaymentError -> {
                    addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_FAILED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants.ERROR_ID_PARAM
                                    to e.error.errorId,
                                HeadlessUniversalCheckoutAnalyticsConstants.ERROR_DESCRIPTION_PARAM
                                    to e.error.description
                            )
                        )
                    )
                    checkoutListener?.onFailed(e.error, e.data)
                }

                is CheckoutEvent.CheckoutError -> {
                    headlessUniversalCheckout?.addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CHECKOUT_FAILED,
                            mapOf(
                                HeadlessUniversalCheckoutAnalyticsConstants.ERROR_ID_PARAM
                                    to e.error.errorId,
                                HeadlessUniversalCheckoutAnalyticsConstants.ERROR_DESCRIPTION_PARAM
                                    to e.error.description
                            )
                        )
                    )
                    checkoutListener?.onFailed(e.error)
                }

                is CheckoutEvent.ClientSessionUpdateSuccess -> {
                    headlessUniversalCheckout?.addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants.ON_CLIENT_SESSION_UPDATED
                        )
                    )
                    checkoutListener?.onClientSessionUpdated(e.data)
                }

                is CheckoutEvent.ClientSessionUpdateStarted -> {
                    headlessUniversalCheckout?.addAnalyticsEvent(
                        SdkFunctionParams(
                            HeadlessUniversalCheckoutAnalyticsConstants
                                .ON_BEFORE_CLIENT_SESSION_UPDATED
                        )
                    )
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
                HeadlessUniversalCheckoutAnalyticsConstants.SET_CHECKOUT_LISTENER_METHOD
            )
        )
        subscription?.unregister()
        subscription = EventBus.subscribe(eventBusListener)
        this.checkoutListener = listener
    }

    override fun setCheckoutUiListener(uiListener: PrimerHeadlessUniversalCheckoutUiListener) {
        headlessUniversalCheckout?.addAnalyticsEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.SET_CHECKOUT_UI_LISTENER_METHOD
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
                    HeadlessUniversalCheckoutAnalyticsConstants.START_METHOD,
                    mapOf(
                        HeadlessUniversalCheckoutAnalyticsConstants.SETTINGS_PARAM to
                            this.config?.settings.toString(),
                        HeadlessUniversalCheckoutAnalyticsConstants.CLIENT_TOKEN_PARAM to
                            clientToken
                    )
                )
            )
        } catch (expected: Exception) {
            emitError(DefaultErrorMapper().getPrimerError(expected))
        }
    }

    override fun cleanup() {
        headlessUniversalCheckout?.addAnalyticsEvent(
            SdkFunctionParams(HeadlessUniversalCheckoutAnalyticsConstants.CLEANUP_METHOD)
        )
        headlessUniversalCheckout?.clear(null)
        headlessUniversalCheckout = null
        checkoutListener = null
        uiListener = null
        subscription?.unregister()
        subscription = null

        DISdkContext.sdkContainer?.let { sdkContainer ->
            sdkContainer.resolve<DefaultHeadlessManagerDelegate>().reset()
            sdkContainer.resolve<RawDataDelegate<*>>().reset()
            sdkContainer.resolve<RawDataDelegate<*>>(PaymentMethodType.PAYMENT_CARD.name)
                .reset()
        }
    }

    internal fun emitError(error: PrimerError) {
        when (getConfig().settings.paymentHandling) {
            PrimerPaymentHandling.AUTO -> eventBusListener.onEvent(
                CheckoutEvent.CheckoutPaymentError(
                    error
                )
            )

            PrimerPaymentHandling.MANUAL -> eventBusListener.onEvent(
                CheckoutEvent.CheckoutError(
                    error
                )
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
        DISdkContext.sdkContainer?.let { sdkContainer ->
            sdkContainer.resolve<DefaultHeadlessManagerDelegate>().reset()
            sdkContainer.resolve<RawDataDelegate<*>>().reset()
            sdkContainer.resolve<RawDataDelegate<*>>(PaymentMethodType.PAYMENT_CARD.name)
                .reset()
            sdkContainer.resolve<VaultManagerDelegate>().reset()
            sdkContainer.clear()
        }
        DISdkContext.init(config, context.applicationContext)
        // refresh the instances
        headlessUniversalCheckout = DISdkContext.sdkContainer?.resolve()
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
