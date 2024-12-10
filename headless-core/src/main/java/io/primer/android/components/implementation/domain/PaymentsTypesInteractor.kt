package io.primer.android.components.implementation.domain

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.components.currencyformat.domain.interactors.FetchCurrencyFormatDataInteractor
import io.primer.android.components.implementation.domain.handler.AvailablePaymentMethodsHandler
import io.primer.android.components.implementation.domain.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.flatMap
import io.primer.android.core.extensions.zipWith
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PaymentsTypesInteractor(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val paymentMethodMapper: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
    private val fetchCurrencyFormatDataInteractor: FetchCurrencyFormatDataInteractor,
    private val availablePaymentMethodsHandler: AvailablePaymentMethodsHandler,
    private val primerSettings: PrimerSettings,
    private val logReporter: LogReporter,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<Unit, None>() {

    override suspend fun performAction(params: None): Result<Unit> {
        val cachePolicy = when (primerSettings.clientSessionCachingEnabled) {
            true -> CachePolicy.CacheFirst
            false -> CachePolicy.ForceNetwork
        }
        return configurationInteractor(
            ConfigurationParams(cachePolicy = cachePolicy)
        ).zipWith(fetchCurrencyFormatDataInteractor.invoke(None)) { configuration, _ -> configuration }
            .flatMap { configuration ->
                paymentMethodModulesInteractor(None)
                    .map { it.descriptors.map { it.config } }
                    .map { configs -> configuration to configs }
            }.map { configurations ->
                val paymentMethods = configurations.second.map { config ->
                    paymentMethodMapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(
                        config.type
                    )
                }
                availablePaymentMethodsHandler.invoke(paymentMethods = paymentMethods)
                logReporter.info("Headless Universal Checkout initialized successfully.")
            }.onFailure { throwable ->
                logReporter.error(CONFIGURATION_ERROR, throwable = throwable)
            }.map { }
    }

    private companion object {
        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a configuration missing. Please ensure" +
                " that you have called PrimerHeadlessUniversalCheckout start method" +
                " and you have received onClientSessionSetupSuccessfully callback before" +
                " calling this method."
    }
}
