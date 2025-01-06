package io.primer.android.paypal.implementation.tokenization.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.flatMap
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.payments.core.tokenization.presentation.composable.PaymentMethodTokenizationCollectorDelegate
import io.primer.android.payments.core.tokenization.presentation.composable.PaymentMethodTokenizationCollectorParams
import io.primer.android.paypal.implementation.composer.presentation.RedirectLauncherParams
import io.primer.android.paypal.implementation.configuration.domain.PaypalConfigurationInteractor
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfigParams
import io.primer.android.paypal.implementation.tokenization.domain.PaypalCreateBillingAgreementInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalCreateOrderInteractor
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateBillingAgreementParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal data class PaypalTokenizationCollectorParams(val primerSessionIntent: PrimerSessionIntent) :
    PaymentMethodTokenizationCollectorParams

internal class PaypalTokenizationCollectorDelegate(
    private val configurationInteractor: PaypalConfigurationInteractor,
    private val createOrderInteractor: PaypalCreateOrderInteractor,
    private val createBillingAgreementInteractor: PaypalCreateBillingAgreementInteractor,
) : PaymentMethodTokenizationCollectorDelegate<PaypalTokenizationCollectorParams>, UiEventable {
    private val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    override suspend fun startDataCollection(params: PaypalTokenizationCollectorParams): Result<Unit> {
        return configurationInteractor(PaypalConfigParams(sessionIntent = params.primerSessionIntent))
            .flatMap { configuration ->
                when (configuration) {
                    is PaypalConfig.PaypalCheckoutConfiguration ->
                        createOrderInteractor(
                            PaypalCreateOrderParams(
                                paymentMethodConfigId = configuration.paymentMethodConfigId,
                                amount = configuration.amount,
                                currencyCode = configuration.currencyCode,
                                successUrl = configuration.successUrl,
                                cancelUrl = configuration.cancelUrl,
                            ),
                        ).map { order ->
                            PaymentMethodLauncherParams(
                                paymentMethodType = PaymentMethodType.PAYPAL.name,
                                sessionIntent = params.primerSessionIntent,
                                RedirectLauncherParams(
                                    url = order.approvalUrl,
                                    successUrl = order.successUrl,
                                    paymentMethodType = PaymentMethodType.PAYPAL.name,
                                    paymentMethodConfigId = configuration.paymentMethodConfigId,
                                    sessionIntent = params.primerSessionIntent,
                                ),
                            )
                        }

                    is PaypalConfig.PaypalVaultConfiguration ->
                        createBillingAgreementInteractor(
                            PaypalCreateBillingAgreementParams(
                                paymentMethodConfigId = configuration.paymentMethodConfigId,
                                successUrl = configuration.successUrl,
                                cancelUrl = configuration.cancelUrl,
                            ),
                        ).map { order ->
                            PaymentMethodLauncherParams(
                                paymentMethodType = PaymentMethodType.PAYPAL.name,
                                sessionIntent = params.primerSessionIntent,
                                RedirectLauncherParams(
                                    url = order.approvalUrl,
                                    successUrl = order.successUrl,
                                    paymentMethodType = PaymentMethodType.PAYPAL.name,
                                    paymentMethodConfigId = configuration.paymentMethodConfigId,
                                    sessionIntent = params.primerSessionIntent,
                                ),
                            )
                        }
                }
            }.map { launcherParams ->
                _uiEvent.emit(
                    ComposerUiEvent.Navigate(launcherParams),
                )
            }
    }
}
