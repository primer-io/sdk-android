package io.primer.android.events

import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.CheckoutDismissInfo
import io.primer.android.model.dto.PaymentMethodToken

abstract class CheckoutEvent(
  val type: CheckoutEventType,
  val public: Boolean
) {
  abstract class PublicCheckoutEvent(type: CheckoutEventType) : CheckoutEvent(type, true)
  abstract class PrivateCheckoutEvent(type: CheckoutEventType) : CheckoutEvent(type, false)

  class TokenizationSuccess(val data: PaymentMethodToken) :
    PublicCheckoutEvent(CheckoutEventType.TOKENIZE_SUCCESS)

  class TokenizationError(val data: APIError) :
    PublicCheckoutEvent(CheckoutEventType.TOKENIZE_ERROR)

  class TokenAddedToVault(val data: PaymentMethodToken) :
    PublicCheckoutEvent(CheckoutEventType.TOKEN_ADDED_TO_VAULT)

  class TokenRemovedFromVault(val data: PaymentMethodToken) :
    PublicCheckoutEvent(CheckoutEventType.TOKEN_REMOVED_FROM_VAULT)

  class Dismissed(val data: CheckoutDismissInfo) :
    PublicCheckoutEvent(CheckoutEventType.DISMISS)
}
