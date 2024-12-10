package io.primer.android.googlepay.implementation.payment.delegate

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.googlepay.implementation.payment.resume.handler.GooglePayResumeDecision
import io.primer.android.googlepay.implementation.payment.resume.handler.GooglePayResumeHandler
import io.primer.android.paymentmethods.common.InitialLauncherParams
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.paymentmethods.core.composer.composable.UiEventable
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.processor3ds.domain.model.Processor3DS
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal data class ThreeDsInitialLauncherParams(
    val supportedThreeDsProtocolVersions: List<String>
) : InitialLauncherParams

internal data class ProcessorThreeDsInitialLauncherParams(
    val processor3DS: Processor3DS
) : InitialLauncherParams

internal class GooglePayPaymentDelegate(
    paymentMethodTokenHandler: PaymentMethodTokenHandler,
    resumePaymentHandler: PaymentResumeHandler,
    successHandler: CheckoutSuccessHandler,
    errorHandler: CheckoutErrorHandler,
    baseErrorResolver: BaseErrorResolver,
    val resumeHandler: GooglePayResumeHandler
) : PaymentMethodPaymentDelegate(
    paymentMethodTokenHandler,
    resumePaymentHandler,
    successHandler,
    errorHandler,
    baseErrorResolver
),
    UiEventable {

    private val _uiEvent = MutableSharedFlow<ComposerUiEvent>()
    override val uiEvent: SharedFlow<ComposerUiEvent> = _uiEvent
    override suspend fun handleNewClientToken(clientToken: String, payment: Payment?): Result<Unit> {
        return resumeHandler.continueWithNewClientToken(clientToken).mapSuspendCatching { decision ->
            when (decision) {
                is GooglePayResumeDecision.GooglePayNative3dsResumeDecision -> _uiEvent.emit(
                    ComposerUiEvent.Navigate(
                        PaymentMethodLauncherParams(
                            paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
                            sessionIntent = PrimerSessionIntent.CHECKOUT,
                            ThreeDsInitialLauncherParams(
                                supportedThreeDsProtocolVersions = decision.supportedThreeDsProtocolVersions
                            )
                        )
                    )
                )

                is GooglePayResumeDecision.GooglePayProcessor3dsResumeDecision -> _uiEvent.emit(
                    ComposerUiEvent.Navigate(
                        PaymentMethodLauncherParams(
                            paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
                            sessionIntent = PrimerSessionIntent.CHECKOUT,
                            ProcessorThreeDsInitialLauncherParams(
                                processor3DS = decision.processor3DS
                            )
                        )
                    )
                )
            }
        }
    }
}
