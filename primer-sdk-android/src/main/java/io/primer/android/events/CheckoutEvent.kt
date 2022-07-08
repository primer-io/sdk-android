package io.primer.android.events

import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.configuration.models.PrimerPaymentMethodType
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.data.token.model.ClientTokenIntent
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.model.CheckoutExitInfo
import io.primer.android.model.CheckoutExitReason
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.payment.processor3ds.Processor3DS
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType

internal sealed class CheckoutEvent(
    val type: CheckoutEventType,
) {

    class TokenizationSuccess(
        val data: PrimerPaymentMethodTokenData,
        val resumeHandler: PrimerResumeDecisionHandler,
    ) : CheckoutEvent(CheckoutEventType.TOKENIZE_SUCCESS)

    class ResumeSuccess(
        val resumeToken: String,
        val resumeHandler: PrimerResumeDecisionHandler,
    ) :
        CheckoutEvent(CheckoutEventType.RESUME_SUCCESS)

    class ResumeSuccessInternal(
        val resumeToken: String,
        val resumeHandler: PrimerResumeDecisionHandler,
    ) :
        CheckoutEvent(CheckoutEventType.RESUME_SUCCESS_INTERNAL)

    class Exit(val data: CheckoutExitInfo) :
        CheckoutEvent(CheckoutEventType.EXIT)

    class DismissInternal(val data: CheckoutExitReason) :
        CheckoutEvent(CheckoutEventType.DISMISS_INTERNAL)

    class ShowSuccess(val delay: Int = 3000, val successType: SuccessType) :
        CheckoutEvent(CheckoutEventType.SHOW_SUCCESS)

    class ShowError(
        val delay: Int = 3000,
        val errorType: ErrorType,
        val message: String? = null
    ) :
        CheckoutEvent(CheckoutEventType.SHOW_ERROR)

    class PaymentCreateStarted(
        val data: PrimerPaymentMethodData,
        val createPaymentHandler: PrimerPaymentCreationDecisionHandler
    ) : CheckoutEvent(CheckoutEventType.PAYMENT_STARTED)

    class PaymentCreateStartedHUC(
        val data: PrimerPaymentMethodData,
        val createPaymentHandler: PrimerPaymentCreationDecisionHandler
    ) : CheckoutEvent(CheckoutEventType.PAYMENT_STARTED)

    class PaymentSuccess(
        val data: PrimerCheckoutData,
    ) : CheckoutEvent(CheckoutEventType.PAYMENT_SUCCESS)

    class CheckoutError(
        val error: PrimerError,
        val errorHandler: PrimerErrorDecisionHandler? = null
    ) : CheckoutEvent(CheckoutEventType.CHECKOUT_MANUAL_ERROR)

    class CheckoutPaymentError(
        val error: PrimerError,
        val data: PrimerCheckoutData? = null,
        val errorHandler: PrimerErrorDecisionHandler? = null
    ) : CheckoutEvent(CheckoutEventType.CHECKOUT_AUTO_ERROR)

    class ClientSessionUpdateStarted :
        CheckoutEvent(CheckoutEventType.CLIENT_SESSION_UPDATE_STARTED)

    class ClientSessionUpdateSuccess(val data: PrimerClientSession) :
        CheckoutEvent(CheckoutEventType.CLIENT_SESSION_UPDATE_SUCCESS)

    class TokenAddedToVaultInternal(val data: PrimerPaymentMethodTokenData) :
        CheckoutEvent(CheckoutEventType.TOKEN_ADDED_TO_VAULT)

    class PaymentContinue(
        val data: PrimerPaymentMethodTokenData,
        val resumeHandler: PrimerResumeDecisionHandler
    ) :
        CheckoutEvent(CheckoutEventType.PAYMENT_CONTINUE)

    class PaymentContinueHUC(
        val data: PrimerPaymentMethodTokenData,
        val resumeHandler: PrimerResumeDecisionHandler
    ) :
        CheckoutEvent(CheckoutEventType.PAYMENT_CONTINUE_HUC)

    internal class Start3DS(
        val processor3DSData: Processor3DS? = null
    ) : CheckoutEvent(CheckoutEventType.START_3DS)

    class StartAsyncRedirectFlow(
        val title: String,
        val paymentMethodType: PaymentMethodType,
        val redirectUrl: String,
        val statusUrl: String,
    ) : CheckoutEvent(CheckoutEventType.START_ASYNC_REDIRECT_FLOW)

    internal class StartAsyncFlow(
        val clientTokenIntent: ClientTokenIntent,
        val statusUrl: String,
        val paymentMethodType: PaymentMethodType,
    ) : CheckoutEvent(CheckoutEventType.START_ASYNC_FLOW)

    // components helpers
    class ConfigurationSuccess(
        val paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    ) : CheckoutEvent(CheckoutEventType.CONFIGURATION_SUCCESS)

    class TokenizationStarted(val paymentMethodType: PrimerPaymentMethodType) :
        CheckoutEvent(CheckoutEventType.TOKENIZE_STARTED)

    class PreparationStarted(val paymentMethodType: PrimerPaymentMethodType) :
        CheckoutEvent(CheckoutEventType.PREPARATION_STARTED)

    class PaymentMethodPresented(val paymentMethodType: PrimerPaymentMethodType) :
        CheckoutEvent(CheckoutEventType.PAYMENT_METHOD_PRESENTED)
}
