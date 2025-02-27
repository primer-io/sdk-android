package io.primer.android.components

import android.content.Context
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.components.implementation.HeadlessUniversalCheckoutAnalyticsConstants
import io.primer.android.components.implementation.errors.domain.model.HeadlessError
import io.primer.android.components.implementation.presentation.DefaultHeadlessUniversalCheckoutDelegate
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.errors.domain.models.PrimerUnknownError

class PrimerHeadlessUniversalCheckout private constructor() :
    PrimerHeadlessUniversalCheckoutInterface, DISdkComponent {
    private var config: PrimerConfig? = null
    private var headlessUniversalCheckoutDelegate: DefaultHeadlessUniversalCheckoutDelegate? = null
    internal var checkoutListener: PrimerHeadlessUniversalCheckoutListener? = null
    internal var uiListener: PrimerHeadlessUniversalCheckoutUiListener? = null

    override fun setCheckoutListener(listener: PrimerHeadlessUniversalCheckoutListener) {
        headlessUniversalCheckoutDelegate?.addAnalyticsEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.SET_CHECKOUT_LISTENER_METHOD,
            ),
        )
        this.checkoutListener = listener
    }

    override fun setCheckoutUiListener(uiListener: PrimerHeadlessUniversalCheckoutUiListener) {
        headlessUniversalCheckoutDelegate?.addAnalyticsEvent(
            SdkFunctionParams(
                HeadlessUniversalCheckoutAnalyticsConstants.SET_CHECKOUT_UI_LISTENER_METHOD,
            ),
        )
        this.uiListener = uiListener
    }

    override fun start(
        context: Context,
        clientToken: String,
        settings: PrimerSettings?,
        checkoutListener: PrimerHeadlessUniversalCheckoutListener?,
        uiListener: PrimerHeadlessUniversalCheckoutUiListener?,
    ) {
        checkoutListener?.let { setCheckoutListener(it) }
        uiListener?.let { setCheckoutUiListener(it) }

        try {
            initialize(context, clientToken, settings)
            headlessUniversalCheckoutDelegate?.start() ?: run {
                emitError(HeadlessError.InitializationError(INITIALIZATION_ERROR))
            }
            val serializedSettings = this.config?.settings?.let { PrimerSettings.serializer.serialize(it) }
            headlessUniversalCheckoutDelegate?.addAnalyticsEvent(
                SdkFunctionParams(
                    HeadlessUniversalCheckoutAnalyticsConstants.START_METHOD,
                    buildMap {
                        if (serializedSettings != null) {
                            put(HeadlessUniversalCheckoutAnalyticsConstants.SETTINGS_PARAM, serializedSettings)
                        }
                        put(HeadlessUniversalCheckoutAnalyticsConstants.CLIENT_TOKEN_PARAM, clientToken)
                    },
                ),
            )
        } catch (expected: Exception) {
            runCatching {
                resolve<ErrorMapperRegistry>().getPrimerError(expected)
            }.fold(
                onSuccess = { error ->
                    emitError(error)
                },
                onFailure = {
                    emitError(PrimerUnknownError(message = expected.message.orEmpty()))
                },
            )
        }
    }

    override fun cleanup() {
        headlessUniversalCheckoutDelegate?.addAnalyticsEvent(
            SdkFunctionParams(HeadlessUniversalCheckoutAnalyticsConstants.CLEANUP_METHOD),
        )
        headlessUniversalCheckoutDelegate?.clear(null)
        headlessUniversalCheckoutDelegate = null
        checkoutListener = null
        uiListener = null
        DISdkContextInitializer.clearHeadless()
        DISdkContextInitializer.clearDropIn()
    }

    fun emitError(error: PrimerError) {
        when (this.config?.settings?.paymentHandling) {
            PrimerPaymentHandling.AUTO -> this.checkoutListener?.onFailed(error = error, checkoutData = null)
            PrimerPaymentHandling.MANUAL -> this.checkoutListener?.onFailed(error = error)
            null -> error("Unexpected state.")
        }
    }

    fun addAnalyticsEvent(params: BaseAnalyticsParams) {
        headlessUniversalCheckoutDelegate?.addAnalyticsEvent(params)
    }

    private fun initialize(
        context: Context,
        clientToken: String,
        settings: PrimerSettings?,
    ) {
        val newConfig = settings?.let { PrimerConfig(it) } ?: getConfig()
        this.config = newConfig
        verifyClientToken(clientToken)
        newConfig.clientTokenBase64 = clientToken
        newConfig.settings.fromHUC = true
        setupDI(context, newConfig)
    }

    private fun getConfig() = config ?: PrimerConfig()

    private fun setupDI(
        context: Context,
        config: PrimerConfig,
    ) {
        DISdkContextInitializer.initHeadless(config, context.applicationContext)

        // refresh the instances
        headlessUniversalCheckoutDelegate = resolve()
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
