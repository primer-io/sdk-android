package io.primer.android.components.implementation.core.presentation

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import io.primer.android.analytics.domain.models.SdkFunctionParams
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.helpers.PaymentMethodShowedHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler

internal interface PaymentMethodStarter {
    suspend fun start(
        context: Context,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        category: PrimerPaymentMethodManagerCategory,
        onPostStart: () -> Unit = {},
    )
}

internal class DefaultPaymentMethodStarter(
    private val analyticsInteractor: AnalyticsInteractor,
    private val composerRegistry: PaymentMethodComposerRegistry,
    private val providerFactoryRegistry: PaymentMethodProviderFactoryRegistry,
    private val paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
    private val paymentMethodShowedHandler: PaymentMethodShowedHandler,
) : PaymentMethodStarter {
    override suspend fun start(
        context: Context,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        category: PrimerPaymentMethodManagerCategory,
        onPostStart: () -> Unit,
    ) {
        addAnalyticsEvent(
            SdkFunctionParams(
                object {}.javaClass.enclosingMethod?.toGenericString().orEmpty(),
                mapOf(
                    "category" to category.name,
                    "paymentMethodType" to paymentMethodType,
                    "intent" to sessionIntent.name,
                ),
            ),
        )

        composerRegistry.unregister(paymentMethodType)
        val composer = providerFactoryRegistry.create(paymentMethodType, sessionIntent)
        composer?.let { composerRegistry.register(paymentMethodType, it) }

        onPostStart()

        val uiEventable = composer as? UiEventable
        uiEventable?.uiEvent?.collect { event ->
            when (event) {
                is ComposerUiEvent.Navigate -> {
                    (
                        paymentMethodNavigationFactoryRegistry.create(paymentMethodType) as?
                            PaymentMethodContextNavigationHandler
                        )
                        ?.getSupportedNavigators(context)
                        ?.firstOrNull { it.canHandle(event.params) }?.navigate(event.params)
                        ?.also {
                            paymentMethodShowedHandler.handle(paymentMethodType)
                        }
                        ?: println("Navigation handler for ${event.params} not found.")
                }

                else -> Unit
            }
        }
    }

    private suspend fun addAnalyticsEvent(params: BaseAnalyticsParams) = analyticsInteractor(params)
}
