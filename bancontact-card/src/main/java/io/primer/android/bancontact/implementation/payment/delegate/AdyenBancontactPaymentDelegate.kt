package io.primer.android.bancontact.implementation.payment.delegate

import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.implementation.payment.resume.handler.AydenBancontactResumeHandler
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.webRedirectShared.implementation.composer.presentation.WebRedirectLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class AdyenBancontactPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: AydenBancontactResumeHandler,
) : PaymentMethodPaymentDelegate(
        paymentMethodTokenHandler,
        resumePaymentHandler,
        successHandler,
        errorHandler,
        baseErrorResolver,
    ),
    UiEventable {
    private val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent

    override suspend fun handleNewClientToken(
        clientToken: String,
        payment: Payment?,
    ): Result<Unit> {
        return resumeHandler.continueWithNewClientToken(clientToken)
            .mapSuspendCatching { decision ->
                _uiEvent.emit(
                    ComposerUiEvent.Navigate(
                        PaymentMethodLauncherParams(
                            paymentMethodType = decision.paymentMethodType,
                            sessionIntent = PrimerSessionIntent.CHECKOUT,
                            initialLauncherParams =
                                WebRedirectLauncherParams(
                                    decision.title,
                                    decision.paymentMethodType,
                                    decision.redirectUrl,
                                    decision.statusUrl,
                                    decision.deeplinkUrl,
                                ),
                        ),
                    ),
                )
            }
    }
}
