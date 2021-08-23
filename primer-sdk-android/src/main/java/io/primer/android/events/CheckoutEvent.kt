package io.primer.android.events

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
        val completionHandler: (Error?) -> Unit,
    ) :
        PublicCheckoutEvent(CheckoutEventType.TOKENIZE_SUCCESS)

    class TokenizationError(val data: APIError) :
        PublicCheckoutEvent(CheckoutEventType.TOKENIZE_ERROR)

    class TokenAddedToVault(val data: PaymentMethodToken) :
        PublicCheckoutEvent(CheckoutEventType.TOKEN_ADDED_TO_VAULT)

    class TokenRemovedFromVault(val data: PaymentMethodToken) :
        PublicCheckoutEvent(CheckoutEventType.TOKEN_REMOVED_FROM_VAULT)

    class Exit(val data: CheckoutExitInfo) :
        PublicCheckoutEvent(CheckoutEventType.EXIT)

    class ApiError(val data: APIError) : PublicCheckoutEvent(CheckoutEventType.API_ERROR)

    class TokenSelected(val data: PaymentMethodToken) :
        PublicCheckoutEvent(CheckoutEventType.TOKEN_SELECTED)

    internal class ToggleProgressIndicator(val data: Boolean) :
        PrivateCheckoutEvent(CheckoutEventType.TOGGLE_LOADING)

    internal class DismissInternal(val data: CheckoutExitReason) :
        PrivateCheckoutEvent(CheckoutEventType.DISMISS_INTERNAL)

    internal class ShowSuccess(val delay: Int = 3000, val successType: SuccessType) :
        PrivateCheckoutEvent(CheckoutEventType.SHOW_SUCCESS)

    internal class ShowError(val delay: Int = 3000, val errorType: ErrorType) :
        PrivateCheckoutEvent(CheckoutEventType.SHOW_ERROR)

    internal object Start3DS : PrivateCheckoutEvent(CheckoutEventType.START_3DS)

    internal object ClearListeners : PrivateCheckoutEvent(CheckoutEventType.CLEAR_LISTENERS)
}
