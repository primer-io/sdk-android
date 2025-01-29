package io.primer.android

import android.content.Context
import android.content.Intent
import android.util.Log
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.settings.internal.PrimerIntent
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.payments.core.helpers.CheckoutExitHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class Primer private constructor() : PrimerInterface, DISdkComponent {
    internal var listener: PrimerCheckoutListener? = null
    private var config: PrimerConfig = PrimerConfig()

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
                mapOf("settings" to PrimerSettings.serializer.serialize(this.config.settings)),
            ),
        )
    }

    override fun cleanup() {
        addAnalyticsEvent(SdkFunctionParams("cleanup"))
        clearGlobalCache()
        listener = null
    }

    override fun showUniversalCheckout(
        context: Context,
        clientToken: String,
    ) {
        addAnalyticsEvent(SdkFunctionParams("showUniversalCheckout"))
        config.intent = PrimerIntent(PrimerSessionIntent.CHECKOUT)
        show(context, clientToken)
    }

    override fun showVaultManager(
        context: Context,
        clientToken: String,
    ) {
        addAnalyticsEvent(SdkFunctionParams("showVaultManager"))
        config.intent = PrimerIntent(PrimerSessionIntent.VAULT)
        show(context, clientToken)
    }

    override fun showPaymentMethod(
        context: Context,
        clientToken: String,
        paymentMethod: String,
        intent: PrimerSessionIntent,
    ) {
        addAnalyticsEvent(
            SdkFunctionParams(
                "showPaymentMethod",
                mapOf("paymentMethodType" to paymentMethod, "intent" to intent.name),
            ),
        )

        config.intent = PrimerIntent(intent, paymentMethod)
        show(context, clientToken)
    }

    /**
     * Private method to set and subscribe using passed in listener. Clears previous subscriptions.
     */
    private fun setListener(listener: PrimerCheckoutListener) {
        this.listener = null
        this.listener = listener
    }

    /**
     * Private method to instantiate [CheckoutSheetActivity] and initialise the SDK.
     */
    private fun show(
        context: Context,
        clientToken: String,
    ) {
        try {
            setupAndVerifyClientToken(clientToken)
            Intent(context, CheckoutSheetActivity::class.java)
                .apply {
                    putExtra(CheckoutSheetActivity.PRIMER_CONFIG_KEY, config)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
        } catch (expected: Exception) {
            Log.e("Primer", expected.message.toString())
            emitError(PrimerUnknownError(message = expected.message.toString()))
        }
    }

    override fun dismiss(clearListeners: Boolean) {
        addAnalyticsEvent(SdkFunctionParams("dismiss"))
        if (clearListeners) listener = null
        resolve<CheckoutExitHandler>().apply {
            handle()
        }
    }

    private fun addAnalyticsEvent(params: SdkFunctionParams) {
        GlobalScope.launch {
            resolve<AnalyticsInteractor>().invoke(params)
        }
    }

    private fun setupAndVerifyClientToken(clientToken: String) {
        ClientToken.fromString(clientToken)
        this.config.clientTokenBase64 = clientToken
    }

    private fun emitError(error: PrimerError) {
        when (config.settings.paymentHandling) {
            PrimerPaymentHandling.AUTO ->
                listener?.onFailed(
                    error = error,
                    checkoutData = null,
                    errorHandler = null,
                )

            PrimerPaymentHandling.MANUAL ->
                listener?.onFailed(
                    error = error,
                    errorHandler = null,
                )
        }
    }

    private fun clearGlobalCache() =
        runCatching {
            resolve<GlobalCacheConfigurationCacheDataSource>(
                ConfigurationCoreContainer.GLOBAL_CACHED_CONFIGURATION_DI_KEY,
            ).clear()
        }

    companion object {
        /**
         * Singleton instance of [Primer]. Use this to call SDK methods.
         */
        internal val current: Primer by lazy { Primer() }

        @JvmStatic
        val instance: PrimerInterface = current
    }
}
