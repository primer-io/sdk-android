package io.primer.android.events

import io.primer.android.completion.PrimerErrorDecisionHandler
import io.primer.android.completion.PrimerPaymentCreationDecisionHandler
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.model.CheckoutExitInfo
import io.primer.android.model.CheckoutExitReason
import io.primer.android.payment.processor3ds.Processor3DS
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType
import kotlinx.coroutines.CancellationException

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

    class ResumePending(val paymentMethodInfo: PrimerCheckoutAdditionalInfo?) :
        CheckoutEvent(CheckoutEventType.RESUME_PENDING)

    class OnAdditionalInfoReceived(val paymentMethodInfo: PrimerCheckoutAdditionalInfo) :
        CheckoutEvent(CheckoutEventType.ON_ADDITIONAL_INFO_RECEIVED)

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

    class PaymentSuccess(val data: PrimerCheckoutData) :
        CheckoutEvent(CheckoutEventType.PAYMENT_SUCCESS)

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

    class Start3DS(
        val processor3DSData: Processor3DS? = null
    ) : CheckoutEvent(CheckoutEventType.START_3DS)

    class StartAsyncRedirectFlow(
        val title: String,
        val paymentMethodType: String,
        val redirectUrl: String,
        val statusUrl: String,
        val deeplinkUrl: String
    ) : CheckoutEvent(CheckoutEventType.START_ASYNC_REDIRECT_FLOW)

    class StartAsyncFlow(
        val clientTokenIntent: String,
        val statusUrl: String,
        val paymentMethodType: String,
    ) : CheckoutEvent(CheckoutEventType.START_ASYNC_FLOW)

    @Suppress("LongParameterList")
    class StartIPay88Flow(
        val clientTokenIntent: String,
        val statusUrl: String,
        val paymentMethodType: String,
        val paymentId: String,
        val paymentMethod: Int,
        val merchantCode: String,
        val amount: String,
        val referenceNumber: String,
        val prodDesc: String,
        val currencyCode: String,
        val countryCode: String?,
        val customerId: String?,
        val customerEmail: String?,
        val backendCallbackUrl: String,
        val deeplinkUrl: String
    ) : CheckoutEvent(CheckoutEventType.START_ASYNC_FLOW)

    class StartVoucherFlow(
        val clientTokenIntent: String,
        val statusUrl: String,
        val paymentMethodType: String,
    ) : CheckoutEvent(CheckoutEventType.START_VOUCHER_FLOW)

    object AsyncFlowRedirect : CheckoutEvent(CheckoutEventType.ASYNC_FLOW_REDIRECT)

    object AsyncFlowPollingError : CheckoutEvent(CheckoutEventType.ASYNC_FLOW_POLLING_ERROR)

    data class AsyncFlowCancelled(val exception: CancellationException? = null) :
        CheckoutEvent(CheckoutEventType.ASYNC_FLOW_CANCELLED)

    // components helpers
    class ConfigurationSuccess(
        val paymentMethods: List<PrimerHeadlessUniversalCheckoutPaymentMethod>
    ) : CheckoutEvent(CheckoutEventType.CONFIGURATION_SUCCESS)

    class TokenizationStarted(val paymentMethodType: String) :
        CheckoutEvent(CheckoutEventType.TOKENIZE_STARTED)

    class PreparationStarted(val paymentMethodType: String) :
        CheckoutEvent(CheckoutEventType.PREPARATION_STARTED)

    class PaymentMethodPresented(val paymentMethodType: String) :
        CheckoutEvent(CheckoutEventType.PAYMENT_METHOD_PRESENTED)

    class HucValidationError(val errors: List<PrimerInputValidationError>) :
        CheckoutEvent(CheckoutEventType.HUC_VALIDATION_ERROR)

    class HucMetadataChanged(val metadata: PrimerPaymentMethodMetadata) :
        CheckoutEvent(CheckoutEventType.HUC_METADATA_CHANGED)
}
