package io.primer.android.ipay88.implementation.payment.presentation.delegate.presentation

import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.ipay88.implementation.composer.presentation.RedirectLauncherParams
import io.primer.android.ipay88.implementation.payment.resume.handler.IPay88ResumeHandler
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class IPay88PaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    private val resumeHandler: IPay88ResumeHandler,
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
        return resumeHandler.continueWithNewClientToken(
            clientToken,
        ).mapSuspendCatching { decision ->
            _uiEvent.emit(
                ComposerUiEvent.Navigate(
                    PaymentMethodLauncherParams(
                        decision.paymentMethodType,
                        decision.sessionIntent,
                        RedirectLauncherParams(
                            statusUrl = decision.statusUrl,
                            iPayPaymentId = decision.iPayPaymentId,
                            iPayMethod = decision.iPayMethod,
                            merchantCode = decision.merchantCode,
                            actionType = decision.actionType,
                            amount = decision.amount,
                            referenceNumber = decision.referenceNumber,
                            prodDesc = decision.prodDesc,
                            currencyCode = decision.currencyCode,
                            countryCode = decision.countryCode,
                            customerName = decision.customerName,
                            customerEmail = decision.customerEmail,
                            remark = decision.remark,
                            backendCallbackUrl = decision.backendCallbackUrl,
                            deeplinkUrl = decision.deeplinkUrl,
                            errorCode = decision.errorCode,
                            paymentMethodType = decision.paymentMethodType,
                            sessionIntent = decision.sessionIntent,
                        ),
                    ),
                ),
            )
        }
    }
}
