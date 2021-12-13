package io.primer.android.events

import io.primer.android.completion.ActionResumeHandler
import io.primer.android.completion.ResumeHandler
import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutExitInfo
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.ui.fragments.ErrorType
import io.primer.android.ui.fragments.SuccessType

sealed class CheckoutEvent(
    val type: CheckoutEventType,
    val public: Boolean,
) {

    abstract class PublicCheckoutEvent(type: CheckoutEventType) : CheckoutEvent(type, true)
    abstract class PrivateCheckoutEvent(type: CheckoutEventType) : CheckoutEvent(type, false)

    class TokenizationSuccess(
        val data: PaymentMethodToken,
        val resumeHandler: ResumeHandler,
    ) :
        PublicCheckoutEvent(CheckoutEventType.TOKENIZE_SUCCESS)

    class TokenizationError(val data: APIError) :
        PublicCheckoutEvent(CheckoutEventType.TOKENIZE_ERROR)

    class TokenAddedToVault(val data: PaymentMethodToken) :
        PublicCheckoutEvent(CheckoutEventType.TOKEN_ADDED_TO_VAULT)

    class TokenRemovedFromVault(val data: PaymentMethodToken) :
        PublicCheckoutEvent(CheckoutEventType.TOKEN_REMOVED_FROM_VAULT)

    class ResumeSuccess(
        val resumeToken: String,
        val resumeHandler: ResumeHandler,
    ) :
        PublicCheckoutEvent(CheckoutEventType.RESUME_SUCCESS)

    class ResumeError(val data: APIError) :
        PublicCheckoutEvent(CheckoutEventType.RESUME_ERR0R)

    class Exit(val data: CheckoutExitInfo) :
        PublicCheckoutEvent(CheckoutEventType.EXIT)

    class ApiError(val data: APIError) : PublicCheckoutEvent(CheckoutEventType.API_ERROR)

    class TokenSelected(
        val data: PaymentMethodToken,
        val resumeHandler: ResumeHandler,
    ) :
        PublicCheckoutEvent(CheckoutEventType.TOKEN_SELECTED)

    class OnClientSessionActions(
        val data: ClientSessionActionsRequest,
        val resumeHandler: ActionResumeHandler,
    ) : PublicCheckoutEvent(CheckoutEventType.ON_CLIENT_SESSION_ACTIONS)

    internal class ToggleProgressIndicator(val data: Boolean) :
        PrivateCheckoutEvent(CheckoutEventType.TOGGLE_LOADING)

    internal class DismissInternal(val data: CheckoutExitReason) :
        PrivateCheckoutEvent(CheckoutEventType.DISMISS_INTERNAL)

    internal class ShowSuccess(val delay: Int = 3000, val successType: SuccessType) :
        PrivateCheckoutEvent(CheckoutEventType.SHOW_SUCCESS)

    internal class ShowError(val delay: Int = 3000, val errorType: ErrorType) :
        PrivateCheckoutEvent(CheckoutEventType.SHOW_ERROR)

    internal object Start3DS : PrivateCheckoutEvent(CheckoutEventType.START_3DS)

    internal class StartAsyncFlow(val redirectUrl: String, val statusUrl: String) :
        PrivateCheckoutEvent(CheckoutEventType.START_ASYNC_FLOW)
}
