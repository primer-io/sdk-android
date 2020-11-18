package io.primer.android.payment_methods

import io.primer.android.session.ClientSession

abstract class PaymentMethod(session: ClientSession): ITokenizable {
  abstract var id: String

  protected var session = session
  protected lateinit var config: PaymentMethodRemoteConfig

  fun initialize(config: PaymentMethodRemoteConfig) {
    this.config = config
  }
}
