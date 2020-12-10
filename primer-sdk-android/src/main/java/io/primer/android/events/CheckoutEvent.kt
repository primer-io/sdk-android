package io.primer.android.events

import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.PaymentMethodToken

internal abstract class CheckoutEvent(
  val type: CheckoutEventType,
) {
  class TokenizationSuccess(val data: PaymentMethodToken) :
    CheckoutEvent(CheckoutEventType.TOKENIZE_SUCCESS)

  class TokenAddedToVault(val data: PaymentMethodToken) :
    CheckoutEvent(CheckoutEventType.TOKEN_ADDED_TO_VAULT)

  class TokenRemovedFromVault(val data: PaymentMethodToken) :
    CheckoutEvent(CheckoutEventType.TOKEN_REMOVED_FROM_VAULT)

  class TokenizationError(val data: APIError) :
    CheckoutEvent(CheckoutEventType.TOKENIZE_ERROR)
}
