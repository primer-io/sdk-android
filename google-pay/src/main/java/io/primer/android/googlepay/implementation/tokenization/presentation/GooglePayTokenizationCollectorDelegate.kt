package io.primer.android.googlepay.implementation.tokenization.presentation

import io.primer.android.googlepay.implementation.composer.ui.navigation.launcher.GooglePayActivityLauncherParams
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationInteractor
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import io.primer.android.payments.core.tokenization.presentation.composable.NoOpPaymentMethodTokenizationCollectorParams
import io.primer.android.payments.core.tokenization.presentation.composable.PaymentMethodTokenizationCollectorDelegate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class GooglePayTokenizationCollectorDelegate(
    private val configurationInteractor: GooglePayConfigurationInteractor
) : PaymentMethodTokenizationCollectorDelegate<NoOpPaymentMethodTokenizationCollectorParams>, UiEventable {

    private val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    override suspend fun startDataCollection(params: NoOpPaymentMethodTokenizationCollectorParams): Result<Unit> {
        return configurationInteractor(NoOpPaymentMethodConfigurationParams).map { configuration ->
            _uiEvent.emit(
                ComposerUiEvent.Navigate(
                    GooglePayActivityLauncherParams(
                        environment = configuration.environment,
                        gatewayMerchantId = configuration.gatewayMerchantId,
                        merchantName = configuration.merchantName,
                        totalPrice = configuration.totalPrice,
                        countryCode = configuration.countryCode,
                        currencyCode = configuration.currencyCode,
                        allowedCardNetworks = configuration.allowedCardNetworks,
                        allowedCardAuthMethods = configuration.allowedCardAuthMethods,
                        billingAddressRequired = configuration.billingAddressRequired,
                        shippingOptions = configuration.shippingOptions,
                        shippingAddressParameters = configuration.shippingAddressParameters,
                        requireShippingMethod = configuration.requireShippingMethod,
                        emailAddressRequired = configuration.emailAddressRequired
                    )
                )
            )
        }
    }
}
