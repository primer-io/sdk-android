package io.primer.android.vault.implementation.vaultedMethods.presentation.delegate

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.VaultedPaymentMethodComponent
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.vault.implementation.composer.presentation.DefaultVaultedPaymentMethodComponent
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class VaultManagerComposerDelegate(
    private val paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
    private val composerRegistry: VaultedPaymentMethodComposerRegistry,
    private val providerFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry,
    private val context: Context,
    private val paymentDelegateProvider: (paymentMethodType: String?) -> PaymentMethodPaymentDelegate
) {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    suspend fun handlePaymentMethod(paymentMethodToken: PrimerPaymentMethodTokenData): Result<PaymentDecision> {
        val paymentMethodType = paymentMethodToken.paymentMethodType.orEmpty()

        composerRegistry.unregister(id = paymentMethodType)
        val composer = resolvePaymentMethodComposer(paymentMethodType = paymentMethodType)
        composer.let { composerRegistry.register(paymentMethodType, it) }

        composer.apply {
            scope.launch {
                subscribeToEvents(paymentMethodType = paymentMethodType)
            }
            start(paymentMethodType = paymentMethodType, sessionIntent = PrimerSessionIntent.CHECKOUT)
            return paymentDelegate.handlePaymentMethodToken(
                paymentMethodTokenData = paymentMethodToken,
                primerSessionIntent = PrimerSessionIntent.CHECKOUT
            ).onFailure { throwable ->
                paymentDelegate.handleError(throwable = throwable)
            }
        }
    }

    private fun resolvePaymentMethodComposer(paymentMethodType: String): VaultedPaymentMethodComponent {
        val providedComposer = providerFactoryRegistry.create(paymentMethodType, PrimerSessionIntent.CHECKOUT)
        val defaultComposer =
            DefaultVaultedPaymentMethodComponent(paymentDelegate = paymentDelegateProvider(paymentMethodType))
        return (providedComposer ?: defaultComposer) as VaultedPaymentMethodComponent
    }

    private suspend fun PaymentMethodComposer.subscribeToEvents(paymentMethodType: String) {
        val uiEventable = this as? UiEventable
        uiEventable?.uiEvent?.collect { event ->
            when (event) {
                is ComposerUiEvent.Navigate -> {
                    (
                        paymentMethodNavigationFactoryRegistry.create(paymentMethodType) as?
                            PaymentMethodContextNavigationHandler
                        )
                        ?.getSupportedNavigators(context)
                        ?.firstOrNull { it.canHandle(event.params) }?.navigate(event.params)
                        ?: println("Navigation handler for ${event.params} not found.")
                }

                else -> {
                    Unit
                }
            }
        }
    }
}
