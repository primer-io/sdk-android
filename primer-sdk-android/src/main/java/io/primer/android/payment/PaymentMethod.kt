package io.primer.android.payment

import io.primer.android.session.ClientSession

abstract class PaymentMethod(session: ClientSession): ITokenizable {
  abstract val id: String

  abstract val isVaultable: Boolean;

  protected val session = session

  protected val config: PaymentMethodRemoteConfig?
    get() {
      return session.paymentMethods.find {
        it.type == id
      }
    }

  val isConfigured: Boolean
    get() = config != null
}
